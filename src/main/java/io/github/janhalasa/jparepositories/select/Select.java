package io.github.janhalasa.jparepositories.select;

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
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> q = cb.createQuery(entityClass);
        final Root<T> root = q.from(entityClass);
        final CriteriaQuery<T> criteriaQuery = q.select(root);

        applyWhere(criteriaQuery, cb, root);

        if (orderAttrs != null) {
            if (this.predicateAndOrderBuilder != null) {
                throw new IllegalStateException("Cannot use orderAttrs and predicateAndOrderBuilder at the same time.");
            }
            List<OrderBy> orderBys = this.orderAttrs.stream()
                    .map(orderAttrs -> orderAttrs.toOrderBy(root))
                    .collect(Collectors.toList());
            criteriaQuery.orderBy(buildOrderBy(orderBys, cb));
        }

        final TypedQuery<T> typedQuery = em.createQuery(criteriaQuery);

        if (distinct) {
            criteriaQuery.distinct(distinct);
        }

        applyFetch(typedQuery);

        return typedQuery;
    }

    private void applyWhere(CriteriaQuery<T> criteriaQuery, CriteriaBuilder cb, Root<T> root) {
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

    public Optional<T> get() {
        List<T> resultList = this.createQuery().getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        if (resultList.size() > 1) {
            throw new NonUniqueResultException("More than one result found: " + resultList.size());
        }
        return Optional.of(resultList.get(0));
    }

    public T load() {
        return this.createQuery().getSingleResult();
    }

    public List<T> find() {
        return this.createQuery().getResultList();
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
