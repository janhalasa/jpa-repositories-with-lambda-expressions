package io.github.janhalasa.jparepositories;

import io.github.janhalasa.jparepositories.model.PagingParams;
import io.github.janhalasa.jparepositories.model.PredicateAndOrderBuilder;
import io.github.janhalasa.jparepositories.model.PredicateBuilder;
import io.github.janhalasa.jparepositories.model.QueryBuilder;
import io.github.janhalasa.jparepositories.model.QueryParams;
import io.github.janhalasa.jparepositories.model.ResultGraph;
import io.github.janhalasa.jparepositories.select.Select;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.List;
import java.util.Optional;

/**
 * Implements support for lambda queries. The most important class of the project.
 * 
 * @param <T> Entity class
 * @author janhalasa
 */
public abstract class BasicRepository<T, P> {

	private Class<T> entityClass;
	private String entityName;
	private SingularAttribute<? super T, P> pkField;

	private EntityManager em;

	/**
	 * This no-param constructor is here just for CDI. In your code, use the one with parameters.
	 */
	@Deprecated
	protected BasicRepository() {
	}

	protected BasicRepository(
			EntityManager em,
			Class<T> entityClass,
			SingularAttribute<? super T, P> pkField) {
		this.em = em;
		this.entityClass = entityClass;
		this.entityName = JpaUtils.getEntityName(entityClass);
		this.pkField = pkField;
	}

	protected EntityManager em() {
		return this.em;
	}

	protected Class<T> entityClass() {
		return this.entityClass;
	}

	protected String entityName() {
		return this.entityName;
	}

	protected SingularAttribute<? super T, ?> pkField() {
		return this.pkField;
	}

	/**
	 * Creates a TypedQuery which can be further customized by calling its methods such as setMaxResults() or
	 * setFirstResult. To get results, call its getResultList() or getSingleResult() method. Method is private, so it
	 * cannot be overridden - it's used by other methods.
	 */
	private TypedQuery<T> createTypedQuery(
			QueryBuilder<T> queryBuilder,
			ResultGraph<T> resultGraph,
			boolean distinct) {
		final CriteriaBuilder cb = em().getCriteriaBuilder();
		final CriteriaQuery<T> q = cb.createQuery(entityClass);
		final Root<T> root = q.from(entityClass);
		final CriteriaQuery<T> criteriaQuery = q.select(root);
		queryBuilder.build(cb, root, criteriaQuery, false);

		final TypedQuery<T> typedQuery = em.createQuery(criteriaQuery);
		criteriaQuery.distinct(distinct);

		if (resultGraph != null) {
			typedQuery.setHint(resultGraph.getType(), resultGraph.getEntityGraph());
		}

		return typedQuery;
	}

	private TypedQuery<T> createTypedQuery(QueryParams<T> queryParams) {
		return createTypedQuery(
				queryParams.getQueryBuilder(),
				queryParams.getResultGraph(),
				queryParams.isDistinct());
	}

	private TypedQuery<T> createTypedQuery(QueryBuilder<T> queryBuilder, ResultGraph<T> resultGraph) {
		return this.createTypedQuery(queryBuilder, resultGraph, false);
	}

	private TypedQuery<T> createTypedQuery(QueryBuilder<T> queryBuilder) {
		return this.createTypedQuery(queryBuilder, null);
	}

	protected TypedQuery<T> createQuery(
			QueryBuilder<T> queryBuilder,
			ResultGraph<T> resultGraph) {
		return createTypedQuery(queryBuilder, resultGraph);
	}

	protected EntityGraph<T> createEntityGraph(List<Attribute<T, ?>> nodesToAdd) {
		EntityGraph<T> eg = em().createEntityGraph(entityClass);
		nodesToAdd.forEach(eg::addAttributeNodes);
		return eg;
	}

	protected List<T> find(QueryBuilder<T> queryBuilder) {
		return find(queryBuilder, null);
	}

	protected List<T> find(QueryBuilder<T> queryBuilder, ResultGraph<T> resultGraph) {
		return createQuery(queryBuilder, resultGraph).getResultList();
	}

	protected List<T> findWhere(PredicateBuilder<T> predicateBuilder) {
		return this.findWhere(new QueryParams<>(predicateBuilder));
	}

	protected List<T> findWhere(PredicateBuilder<T> predicateBuilder, ResultGraph<T> resultGraph) {
		return this.findWhereOrdered(new QueryParams<>(predicateBuilder, resultGraph));
	}

	protected List<T> findWhere(QueryParams<T> queryParameters) {
		return this.findWhereOrdered(queryParameters);
	}

	protected List<T> findWhereOrdered(PredicateAndOrderBuilder<T> predicateAndOrderBuilder) {
		return findWhereOrdered(new QueryParams<>(predicateAndOrderBuilder));
	}

	protected List<T> findWhereOrdered(
			PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
			ResultGraph<T> resultGraph) {
		return this.findWhereOrdered(new QueryParams<>(
				predicateAndOrderBuilder,
				resultGraph));
	}

	protected List<T> findWhereOrdered(QueryParams<T> queryParameters) {
		return createTypedQuery(queryParameters).getResultList();
	}

	protected List<T> findAll() {
		return this.findAll(null);
	}

	protected List<T> findAll(ResultGraph<T> resultGraph) {
		return this.find((cb, root, criteriaQuery, omitSorting) -> {}, resultGraph);
	}

	protected T loadWhere(QueryParams<T> queryParams) {
		return createTypedQuery(queryParams).getSingleResult();
	}

	protected T loadWhere(PredicateBuilder<T> predicateBuilder, ResultGraph<T> resultGraph) {
		return this.loadWhere(new QueryParams<>(predicateBuilder, resultGraph, false));
	}

	protected T loadWhere(PredicateBuilder<T> predicateBuilder) {
		return loadWhere(new QueryParams<>(predicateBuilder));
	}

	protected T loadByPk(P pkValue) {
		return loadByPk(pkValue, null);
	}

	protected T loadByPk(P pkValue, ResultGraph<T> resultGraph) {
		return this.loadWhere((cb, root) -> cb.equal(root.get(this.pkField), pkValue), resultGraph);
	}

	protected Optional<T> getWhere(QueryParams<T> queryParams) {
		final List<T> results = createTypedQuery(queryParams).getResultList();
		if (results.isEmpty()) {
			return Optional.empty();
		}
		if (results.size() > 1) {
			throw new NonUniqueResultException("There were " + results.size() + " results");
		}
		return Optional.of(results.get(0));
	}

	protected Optional<T> getWhere(PredicateBuilder<T> predicateBuilder, ResultGraph<T> resultGraph) {
		return this.getWhere(new QueryParams<>(predicateBuilder, resultGraph, false));
	}

	protected Optional<T> getWhere(PredicateBuilder<T> predicateBuilder) {
		return getWhere(new QueryParams<>(predicateBuilder));
	}

	protected Optional<T> getByPk(P pkValue) {
		return Optional.ofNullable(this.em().find(this.entityClass(), pkValue));
	}

	protected Optional<T> getByPk(P pkValue, ResultGraph<T> resultGraph) {
		return this.getWhere((cb, root) -> cb.equal(root.get(this.pkField), pkValue), resultGraph);
	}

	protected long countWhere(PredicateBuilder<T> predicateBuilder) {
		return countWhere(PredicateAndOrderBuilder.of(predicateBuilder).toQueryBuilder(false), false);
	}

	protected long countWhere(PredicateBuilder<T> predicateBuilder, boolean distinct) {
		return countWhere(PredicateAndOrderBuilder.of(predicateBuilder).toQueryBuilder(false), distinct);
	}

	protected long countWhere(
			QueryBuilder<T> queryBuilder,
			boolean distinct) {
		final CriteriaBuilder cb = em().getCriteriaBuilder();
		final CriteriaQuery<Long> q = cb.createQuery(Long.class);
		final Root<T> root = q.from(entityClass);
		final CriteriaQuery<Long> criteriaQuery = q.select(distinct ? cb.countDistinct(root) : cb.count(root));
		queryBuilder.build(cb, root, criteriaQuery, true);
		return em.createQuery(criteriaQuery)
				.getSingleResult();
	}

	protected ResultPage<T> pageWhere(
			PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
			int pageNumber,
			int pageSize) {
		return pageWhere(new PagingParams<>(
				predicateAndOrderBuilder,
				pageNumber,
				pageSize));
	}

	protected ResultPage<T> pageWhere(
			PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
			int pageNumber,
			int pageSize,
			ResultGraph<T> resultGraph) {
		return pageWhere(new PagingParams<>(
				predicateAndOrderBuilder,
				pageNumber,
				pageSize,
				resultGraph));
	}


	protected ResultPage<T> pageWhere(
			PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
			int pageNumber,
			int pageSize,
			boolean distinct,
			ResultGraph<T> resultGraph) {
		return pageWhere(new PagingParams<>(
				predicateAndOrderBuilder,
				pageNumber,
				pageSize,
				resultGraph,
				distinct
		));
	}

	protected ResultPage<T> pageWhere(PagingParams<T> pagingParams) {
		final int pageNumber = pagingParams.getPageNumber();
		final int pageSize = pagingParams.getPageSize();

		if (pageNumber < 1) {
			throw new IllegalArgumentException("Page number must be 1 or higher: " + pageNumber);
		}
		if (pageSize < 1) {
			throw new IllegalArgumentException("Page size must be 1 or higher: " + pageSize);
		}

		final TypedQuery<T> typedQuery = createTypedQuery(pagingParams.getQueryParams());

		final List<T> resultList = typedQuery
				.setFirstResult((pageNumber - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();

		// Count is always distinct, because we always want to count distinct entities, no matter how JPA composes the SELECT statement.
		// If there are *ToMany joins applied, the count would be incorrect (higher) without the distinct clause.
		final long totalCount = this.countWhere(pagingParams.getQueryParams().getQueryBuilder(), true);

		return new ResultPage<>(totalCount, pageNumber, pageSize, resultList);
	}

	protected void persist(T entity) {
		this.em().persist(entity);
	}

	protected T merge(T entity) {
		return this.em().merge(entity);
	}

	protected void remove(T entity) {
		this.em().remove(entity);
	}

	protected void removeByPk(P entityPk) {
		this.getByPk(entityPk).ifPresent(this::remove);
	}

	protected int removeWhere(PredicateBuilder<T> predicateBuilder) {
		CriteriaBuilder cb = em().getCriteriaBuilder();
		CriteriaDelete<T> delete = cb.createCriteriaDelete(this.entityClass());
		Root<T> root = delete.from(this.entityClass());
		delete.where(predicateBuilder.build(cb, root));
		return em().createQuery(delete).executeUpdate();
	}

	protected Select<T> select() {
		return Select.from(this.entityClass(), this.em());
	}
}
