package io.github.janhalasa.jparepositories;

import io.github.janhalasa.jparepositories.model.OrderBy;
import io.github.janhalasa.jparepositories.model.PredicateAndOrder;
import io.github.janhalasa.jparepositories.model.PredicateAndOrderBuilder;
import io.github.janhalasa.jparepositories.model.PredicateBuilder;
import io.github.janhalasa.jparepositories.model.QueryBuilder;
import io.github.janhalasa.jparepositories.model.ResultGraph;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements support for lambda queries. The most important class of the project.
 * 
 * @param <T> Entity class
 * @author janhalasa
 */
public abstract class BasicRepository<T, P> {

	private Class<T> entityClass;
	private String entityName;
	private SingularAttribute<T, P> pkField;

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
			SingularAttribute<T, P> pkField) {
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

	protected SingularAttribute<T, ?> pkField() {
		return this.pkField;
	}

	/**
	 * Creates a TypedQuery which can be further customized by calling its methods such as setMaxResults() or
	 * setFirstResult. To get results, call its getResultList() or getSingleResult() method. Method is private, so it
	 * cannot be overridden - it's used by other methods.
	 */
	private TypedQuery<T> createTypedQuery(
			QueryBuilder<T> queryBuilder,
			ResultGraph<T> resultGraph) {
		CriteriaBuilder cb = em().getCriteriaBuilder();
		CriteriaQuery<T> q = cb.createQuery(entityClass);
		Root<T> root = q.from(entityClass);
		CriteriaQuery<T> criteriaQuery = q.select(root);
		criteriaQuery = queryBuilder.build(cb, root, criteriaQuery);

		final TypedQuery<T> typedQuery = em.createQuery(criteriaQuery);

		if (resultGraph != null) {
			typedQuery.setHint(resultGraph.getType(), resultGraph.getEntityGraph());
		}

		return typedQuery;
	}

	private TypedQuery<T> createTypedQuery(QueryBuilder<T> queryBuilder) {
		return this.createTypedQuery(queryBuilder, null);
	}

	private static List<Order> buildOrderBy(List<OrderBy> orderByList, CriteriaBuilder cb) {
		return orderByList.stream()
				.map(orderBy -> orderBy.toJpa(cb))
				.collect(Collectors.toList());
	}

	protected TypedQuery<T> createQuery(QueryBuilder<T> queryBuilder) {
		return createTypedQuery(queryBuilder);
	}

	protected TypedQuery<T> createQuery(
			QueryBuilder<T> queryBuilder,
			ResultGraph<T> resultGraph) {
		return createTypedQuery(queryBuilder, resultGraph);
	}

	@SuppressWarnings("unchecked")
	protected EntityGraph<T> createEntityGraph(List<Attribute<T, ?>> nodesToAdd) {
		EntityGraph<T> eg = em().createEntityGraph(entityClass);
		eg.addAttributeNodes(nodesToAdd.toArray(new Attribute[0]));
		return eg;
	}

	protected Predicate[] buildPredicates(CriteriaBuilder cb, Root<T> root, PredicateBuilder<T>[] predicateBuilders) {
		final List<Predicate> predicates = new ArrayList<>();
		if (predicateBuilders != null) {
			for (PredicateBuilder<T> builder : predicateBuilders) {
				predicates.add(builder.build(cb, root));
			}
		}
		return predicates.toArray(new Predicate[0]);
	}

	@SuppressWarnings("unchecked")
	private PredicateBuilder<T>[] toPredicateBuilderArray(PredicateBuilder<T> predicateBuilder) {
		return predicateBuilder != null
				? new PredicateBuilder[]{predicateBuilder}
				: new PredicateBuilder[]{};
	}

	protected List<T> find(QueryBuilder<T> queryBuilder) {
		return find(queryBuilder, null);
	}

	protected List<T> find(QueryBuilder<T> queryBuilder, ResultGraph<T> resultGraph) {
		return createQuery(queryBuilder, resultGraph).getResultList();
	}

	protected List<T> findWhere(PredicateBuilder<T> predicateBuilder) {
		return this.findWhere(predicateBuilder, null);
	}

	protected List<T> findWhere(PredicateBuilder<T> predicateBuilder, ResultGraph<T> resultGraph) {
		return this.findWhereOrdered(
				PredicateAndOrderBuilder.of(predicateBuilder),
				resultGraph);
	}

	protected List<T> findWhereOrdered(PredicateAndOrderBuilder<T> predicateAndOrderBuilder) {
		return findWhereOrdered(predicateAndOrderBuilder, null);
	}

	protected List<T> findWhereOrdered(
			PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
			ResultGraph<T> resultGraph) {
		return createTypedQuery(
				(cb, root, query) -> {
					final PredicateAndOrder predicateAndOrder = predicateAndOrderBuilder.build(cb, root);
					if (predicateAndOrder.getPredicate() != null) {
						query.where(predicateAndOrder.getPredicate());
					}
					if (predicateAndOrder.getOrders() != null) {
						query.orderBy(buildOrderBy(predicateAndOrder.getOrders(), cb));
					}
					return query;
				},
				resultGraph
		).getResultList();
	}

	protected List<T> findAll() {
		return this.findAll(null);
	}

	protected List<T> findAll(ResultGraph<T> resultGraph) {
		return this.find((cb, root, criteriaQuery) -> criteriaQuery, resultGraph);
	}

	@SuppressWarnings("unchecked")
	protected T loadWhere(List<PredicateBuilder<T>> predicateBuilders, ResultGraph<T> resultGraph) {
		return createTypedQuery(
				(cb, root, query) -> (query
						.where(buildPredicates(
								cb,
								root,
								predicateBuilders.toArray(new PredicateBuilder[0])))),
				resultGraph
		).getSingleResult();
	}

	protected T loadWhere(List<PredicateBuilder<T>> predicateBuilders) {
		return loadWhere(predicateBuilders, null);
	}

	protected T loadWhere(PredicateBuilder<T> predicateBuilder) {
		return loadWhere(Collections.singletonList(predicateBuilder));
	}

	protected T loadWhere(PredicateBuilder<T> predicateBuilder, ResultGraph<T> resultGraph) {
		return loadWhere(Collections.singletonList(predicateBuilder), resultGraph);
	}

	protected T loadWhere(PredicateBuilder<T> predicateBuilder1, PredicateBuilder<T> predicateBuilder2) {
		return loadWhere(Arrays.asList(predicateBuilder1, predicateBuilder2));
	}

	protected T loadWhere(
			PredicateBuilder<T> predicateBuilder1,
			PredicateBuilder<T> predicateBuilder2,
			ResultGraph<T> resultGraph) {
		return loadWhere(Arrays.asList(predicateBuilder1, predicateBuilder2), resultGraph);
	}

	protected T loadByPk(P pkValue) {
		return loadByPk(pkValue, null);
	}

	protected T loadByPk(P pkValue, ResultGraph<T> resultGraph) {
		return this.loadWhere((cb, root) -> cb.equal(root.get(this.pkField), pkValue), resultGraph);
	}

	protected Optional<T> getWhere(List<PredicateBuilder<T>> predicateBuilders, ResultGraph<T> resultGraph) {
		@SuppressWarnings("unchecked")
		final List<T> results = createTypedQuery(
				(cb, root, query) -> (query.where(buildPredicates(
						cb,
						root,
						predicateBuilders.toArray(new PredicateBuilder[0])))),
				resultGraph
		).getResultList();
		if (results.isEmpty()) {
			return Optional.empty();
		}
		if (results.size() > 1) {
			throw new NonUniqueResultException("There were " + results.size() + " results");
		}
		return Optional.of(results.get(0));
	}

	protected Optional<T> getWhere(List<PredicateBuilder<T>> predicateBuilders) {
		return getWhere(predicateBuilders, null);
	}

	protected Optional<T> getWhere(PredicateBuilder<T> predicateBuilder) {
		return getWhere(predicateBuilder, (ResultGraph<T>) null);
	}

	protected Optional<T> getWhere(PredicateBuilder<T> predicateBuilder, ResultGraph<T> resultGraph) {
		return getWhere(Collections.singletonList(predicateBuilder), resultGraph);
	}

	protected Optional<T> getWhere(
			PredicateBuilder<T> predicateBuilder1,
			PredicateBuilder<T> predicateBuilder2) {
		return getWhere(predicateBuilder1, predicateBuilder2, null);
	}

	protected Optional<T> getWhere(
			PredicateBuilder<T> predicateBuilder1,
			PredicateBuilder<T> predicateBuilder2,
			ResultGraph<T> resultGraph) {
		return getWhere(Arrays.asList(predicateBuilder1, predicateBuilder2), resultGraph);
	}

	protected Optional<T> getByPk(P pkValue) {
		return Optional.ofNullable(this.em().find(this.entityClass(), pkValue));
	}

	protected Optional<T> getByPk(P pkValue, ResultGraph<T> resultGraph) {
		return this.getWhere((cb, root) -> cb.equal(root.get(this.pkField), pkValue), resultGraph);
	}

	protected long countWhere(PredicateBuilder<T> predicateBuilder) {
		return countWhere(predicateBuilder, false);
	}

	protected long countWhere(
			PredicateBuilder<T> predicateBuilder,
			boolean distinct) {
		CriteriaBuilder cb = em().getCriteriaBuilder();
		CriteriaQuery<Long> q = cb.createQuery(Long.class);
		Root<T> root = q.from(entityClass);
		CriteriaQuery<Long> criteriaQuery = q.select(distinct ? cb.countDistinct(root) : cb.count(root));
		final Predicate predicate = predicateBuilder.build(cb, root);
		if (predicate != null) {
			criteriaQuery = criteriaQuery.where(predicate);
		}
		return em.createQuery(criteriaQuery)
				.getSingleResult();
	}

	protected ResultPage<T> pageWhere(
			PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
			int pageNumber,
			int pageSize) {
		return pageWhere(
				predicateAndOrderBuilder,
				pageNumber,
				pageSize,
				null);
	}

	protected ResultPage<T> pageWhere(
			PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
			int pageNumber,
			int pageSize,
			ResultGraph<T> resultGraph) {
		return pageWhere(
				predicateAndOrderBuilder,
				pageNumber,
				pageSize,
				false,
				resultGraph);
	}

	protected ResultPage<T> pageWhere(
			PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
			int pageNumber,
			int pageSize,
			boolean distinct,
			ResultGraph<T> resultGraph) {

		if (predicateAndOrderBuilder == null) {
			throw new IllegalArgumentException("Pagination requires at least a sort definition.");
		}
		if (pageNumber < 1) {
			throw new IllegalArgumentException("Page number must be 1 or higher: " + pageNumber);
		}
		if (pageSize < 1) {
			throw new IllegalArgumentException("Page size must be 1 or higher: " + pageSize);
		}

		final TypedQuery<T> typedQuery = createTypedQuery(
				(cb, root, query) -> {
					query.distinct(distinct);
					final PredicateAndOrder predicateAndOrder = predicateAndOrderBuilder.build(cb, root);
					if (predicateAndOrder.getPredicate() != null) {
						query.where(predicateAndOrder.getPredicate());
					}
					if (predicateAndOrder.getOrders() == null) {
						throw new IllegalArgumentException("Pagination requires a sort definition " +
								"to be able to produce repeatable results.");
					}
					query.orderBy(buildOrderBy(predicateAndOrder.getOrders(), cb));
					return query;
				},
				resultGraph);

		final List<T> resultList = typedQuery
				.setFirstResult((pageNumber - 1) * pageSize)
				.setMaxResults(pageSize)
				.getResultList();

		// Count is always distinct, because we always want to count distinct entities, no matter how JPA composes the SELECT statement.
		// If there are *ToMany joins applied, the count would be incorrect (higher) without the distinct clause.
		final long totalCount = this.countWhere(
				(cb, root) -> predicateAndOrderBuilder.build(cb, root).getPredicate(), true);

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
}
