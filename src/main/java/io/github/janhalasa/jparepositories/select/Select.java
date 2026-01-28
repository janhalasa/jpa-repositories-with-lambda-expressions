package io.github.janhalasa.jparepositories.select;

import io.github.janhalasa.jparepositories.ResultPage;
import io.github.janhalasa.jparepositories.model.Fetcher;
import io.github.janhalasa.jparepositories.model.OrderAttr;
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
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Select<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Select.class);

    private final Class<T> entityClass;
    private final EntityManager em;
    private boolean distinct = false;
    private PredicateBuilder<T> predicateBuilder = null;
    private PredicateAndOrderBuilder<T> predicateAndOrderBuilder = null;
    private QueryBuilder<T> queryBuilder = null;
    private List<OrderAttr<T>> orderAttrs;
    private ResultGraph<T> resultGraph = null;
    private List<Attribute<T, ?>> nodesToFetch;
    private boolean nodesToFetchOnly = false;
    private Fetcher<T> fetcher;

    public static <T> Select<T> from(Class<T> entityClass, EntityManager em) {
        return new Select<>(entityClass, em);
    }

    private Select(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em = em;
    }

    public Select<T> distinct() {
        this.distinct = true;
        return this;
    }

    /**
     * This method is for methods with distinct as a parameter.
     */
    public Select<T> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public Select<T> where(PredicateBuilder<T> predicateBuilder) {
        if (this.predicateBuilder != null) {
            LOGGER.warn("Overriding previously defined predicate builder");
        }
        if (this.queryBuilder != null) {
            LOGGER.warn("Overriding previously defined query builder");
            this.queryBuilder = null;
        }
        this.predicateBuilder = predicateBuilder;
        return this;
    }

    public Select<T> whereAndOrder(PredicateAndOrderBuilder<T> predicateAndOrderBuilder) {
        if (this.predicateBuilder != null) {
            LOGGER.warn("Overriding previously defined predicate builder");
        }
        if (this.queryBuilder != null) {
            LOGGER.warn("Overriding previously defined query builder");
            this.queryBuilder = null;
        }
        this.predicateAndOrderBuilder = predicateAndOrderBuilder;
        return this;
    }

    public Select<T> where(QueryBuilder<T> queryBuilder) {
        if (this.predicateBuilder != null) {
            LOGGER.warn("Overriding previously defined predicate builder");
            this.predicateBuilder = null;
        }
        if (this.queryBuilder != null) {
            LOGGER.warn("Overriding previously defined query builder");
        }
        this.queryBuilder = queryBuilder;
        return this;
    }

    public Select<T> orderBy(OrderAttr<T> orderAttr) {
        this.orderAttrs = List.of(orderAttr);
        return this;
    }

    public Select<T> orderBy(OrderAttr<T> orderAttr1, OrderAttr<T> orderAttr2) {
        this.orderAttrs = List.of(orderAttr1, orderAttr2);
        return this;
    }

    public Select<T> orderBy(List<OrderAttr<T>> orderAttrs) {
        this.orderAttrs = orderAttrs;
        return this;
    }

    /**
     * Configures the query to use a specific result graph for fetching data.
     * This method overrides any previously defined result graph or nodes to fetch.
     *
     * @param resultGraph the {@code ResultGraph<T>} object that defines the entity graph to be used
     *                    for fetching the query results.
     * @return the current {@code Select<T>} instance, allowing for method chaining to further configure the query.
     */
    public Select<T> fetch(ResultGraph<T> resultGraph) {
        if (this.resultGraph != null) {
            LOGGER.warn("Overriding previously defined result graph");
        }
        if (this.nodesToFetch != null) {
            LOGGER.warn("Overriding previously defined nodes to fetch");
            this.nodesToFetch = null;
        }
        this.resultGraph = resultGraph;
        return this;
    }

    /**
     * Configures a custom fetch strategy for the query using the provided {@code Fetcher}.
     * The {@code Fetcher} is a functional interface which facilitates the customization
     * of fetched associations or attributes by modifying the root entity graph.
     *
     * @param fetcher the {@code Fetcher<T>} implementation that defines the fetch strategy
     *                and specifies the attributes or relationships to be fetched
     *                in the query results.
     * @return the current instance of {@code Select<T>}, enabling method chaining for further query configuration.
     */
    public Select<T> fetch(Fetcher<T> fetcher) {
        this.fetcher = fetcher;
        return this;
    }

    /**
     * Sets a specific list of attributes to be fetched in the query. This method disables fetching of EAGER attributes.
     *
     * @param nodesToFetch the list of attributes represented by `Attribute&lt;T, ?&gt;` objects to be fetched. These attributes
     *                     define specific nodes in the entity graph to include in the fetch operation.
     * @return the current instance of `Select&lt;T&gt;`, enabling method chaining.
     */
    public Select<T> fetchOnly(List<Attribute<T, ?>> nodesToFetch) {
        if (this.resultGraph != null) {
            LOGGER.warn("Overriding previously defined result graph");
            this.resultGraph = null;
        }
        if (this.nodesToFetch != null) {
            LOGGER.warn("Overriding previously defined nodes to fetch");
        }
        this.nodesToFetchOnly = true;
        this.nodesToFetch = nodesToFetch;
        return this;
    }

    /**
     * Sets the list of attributes to be fetched in addition to EAGER associations.
     * This method modifies the internal fetch configuration of the query.
     * It overrides any previously defined nodes to fetch or related result graph settings.
     *
     * @param nodesToFetch the list of attributes to be fetched, represented by `Attribute&lt;T, ?&gt;` objects.
     *                     These attributes define specific nodes in the entity graph to be fetched.
     * @return the current instance of `Select&lt;T&gt;` for method chaining.
     */
    public Select<T> fetchExtra(List<Attribute<T, ?>> nodesToFetch) {
        if (this.resultGraph != null) {
            LOGGER.warn("Overriding previously defined result graph");
            this.resultGraph = null;
        }
        if (this.nodesToFetch != null) {
            LOGGER.warn("Overriding previously defined nodes to fetch");
        }
        this.nodesToFetchOnly = false;
        this.nodesToFetch = nodesToFetch;
        return this;
    }

    public TypedQuery<T> createQuery() {
        return this.createQuery(true, false);
    }

    private TypedQuery<T> createQuery(boolean applyFetch, boolean warnIfNoOrdering) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> q = cb.createQuery(entityClass);
        final Root<T> root = q.from(entityClass);
        final CriteriaQuery<T> criteriaQuery = q.select(root);

        applyWhere(criteriaQuery, cb, root);

        if (orderAttrs != null) {
            if (criteriaQuery.getOrderList() != null && !criteriaQuery.getOrderList().isEmpty()) {
                throw new IllegalStateException("Cannot mix orderAttrs and other ways of setting order.");
            }
            List<OrderBy> orderBys = this.orderAttrs.stream()
                    .map(orderAttrs -> orderAttrs.toOrderBy(root))
                    .collect(Collectors.toList());
            criteriaQuery.orderBy(buildOrderBy(orderBys, cb));
        }

        if (warnIfNoOrdering && (criteriaQuery.getOrderList() == null || criteriaQuery.getOrderList().isEmpty())) {
            LOGGER.warn("No ordering set. This may lead to unpredicable page results.");
        }

        if (applyFetch && this.fetcher != null) {
            // The FetchCreator must be called before creating a TypedQuery, otherwise it has no effect.
            fetcher.create(root);
        }

        final TypedQuery<T> typedQuery = em.createQuery(criteriaQuery);

        if (distinct) {
            criteriaQuery.distinct(distinct);
        }

        if (applyFetch) {
            applyFetch(typedQuery);
        }

        return typedQuery;
    }

    private void applyWhere(CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb, Root<T> root) {
        List<String> appliedTypes = new ArrayList<>(3);
        if (predicateBuilder != null) {
            appliedTypes.add("predicateBuilder");
            criteriaQuery.where(predicateBuilder.build(cb, root));
        }
        if (predicateAndOrderBuilder != null) {
            appliedTypes.add("predicateBuilderAndOrderBuilder");
            PredicateAndOrder predicateAndOrder = predicateAndOrderBuilder.build(cb, root);
            criteriaQuery.where(predicateAndOrder.getPredicate());
            criteriaQuery.orderBy(predicateAndOrder.getOrders().stream()
                    .map(orderBy -> orderBy.toJpa(cb))
                    .collect(Collectors.toList()));
        }
        if (queryBuilder != null) {
            appliedTypes.add("queryBuilder");
            queryBuilder.build(cb, root, criteriaQuery, false);
        }
        if (appliedTypes.size() > 1) {
            throw new IllegalStateException("Only one of " + String.join(", ", appliedTypes) + " can be set");
        }
    }

    private void applyFetch(TypedQuery<T> typedQuery) {
        ResultGraph<T> resultGraph = this.resultGraph;
        if (nodesToFetch != null) {
            EntityGraph<T> entityGraph = createEntityGraph(nodesToFetch);
            this.resultGraph = this.nodesToFetchOnly
                    ? ResultGraph.specifiedAssociationsOnly(entityGraph)
                    : ResultGraph.specifiedAndEagerAssociations(entityGraph);
        }
        if (resultGraph != null) {
            typedQuery.setHint(resultGraph.getType(), resultGraph.getEntityGraph());
        }
    }

    public Optional<T> optional() {
        List<T> resultList = this.createQuery().getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        if (resultList.size() > 1) {
            throw new NonUniqueResultException("More than one result found: " + resultList.size());
        }
        return Optional.of(resultList.get(0));
    }

    public T single() {
        return this.createQuery().getSingleResult();
    }

    public List<T> list() {
        return this.createQuery().getResultList();
    }

    public ResultPage<T> page(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be 1 or higher: " + pageNumber);
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be 1 or higher: " + pageSize);
        }

        final TypedQuery<T> typedQuery = this.createQuery(false, true);

        final List<T> resultList = typedQuery
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        final long totalCount = this.count();

        return new ResultPage<>(totalCount, pageNumber, pageSize, resultList);
    }

    public long count() {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Long> q = cb.createQuery(Long.class);
        final Root<T> root = q.from(entityClass);
        final CriteriaQuery<Long> criteriaQuery = q.select(distinct ? cb.countDistinct(root) : cb.count(root));
        applyWhere(criteriaQuery, cb, root);
        return em.createQuery(criteriaQuery)
                .getSingleResult();
    }

    private EntityGraph<T> createEntityGraph(List<Attribute<T, ?>> nodesToAdd) {
        EntityGraph<T> eg = em.createEntityGraph(entityClass);
        nodesToAdd.forEach(eg::addAttributeNodes);
        return eg;
    }

    private static List<Order> buildOrderBy(List<OrderBy> orderByList, CriteriaBuilder cb) {
        return orderByList.stream()
                .map(orderBy -> orderBy.toJpa(cb))
                .collect(Collectors.toList());
    }
}
