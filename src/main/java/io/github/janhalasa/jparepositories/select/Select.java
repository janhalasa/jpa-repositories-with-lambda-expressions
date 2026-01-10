package io.github.janhalasa.jparepositories.select;

import io.github.janhalasa.jparepositories.model.OrderBy;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Select<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Select.class);

    private final Class<T> entityClass;
    private boolean distinct = false;
    private PredicateBuilder<T> predicateBuilder = null;
    private QueryBuilder<T> queryBuilder = null;
    private List<OrderBy> orderBys;
    private ResultGraph<T> resultGraph = null;
    private List<Attribute<T, ?>> nodesToFetch;
    private boolean nodesToFetchOnly = false;

    public static <T> Select<T> from(Class<T> entityClass) {
        return new Select<>(entityClass);
    }

    private Select(Class<T> entityClass) {
        this.entityClass = entityClass;
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

    public Select<T> orderBy(OrderBy orderBy) {
        this.orderBys = List.of(orderBy);
        return this;
    }

    public Select<T> orderBy(List<OrderBy> orderBys) {
        this.orderBys = orderBys;
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

    public TypedQuery<T> createQuery(EntityManager em) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> q = cb.createQuery(entityClass);
        final Root<T> root = q.from(entityClass);
        final CriteriaQuery<T> criteriaQuery = q.select(root);

        applyWhere(criteriaQuery, cb, root);

        if (orderBys != null) {
            criteriaQuery.orderBy(buildOrderBy(this.orderBys, cb));
        }

        final TypedQuery<T> typedQuery = em.createQuery(criteriaQuery);

        if (distinct) {
            criteriaQuery.distinct(distinct);
        }

        applyFetch(em, typedQuery);

        return typedQuery;
    }

    private void applyWhere(CriteriaQuery<T> criteriaQuery, CriteriaBuilder cb, Root<T> root) {
        if (predicateBuilder != null) {
            criteriaQuery.where(predicateBuilder.build(cb, root));
        }
        if (queryBuilder != null) {
            queryBuilder.build(cb, root, criteriaQuery, false);
        }
    }

    private void applyFetch(EntityManager em, TypedQuery<T> typedQuery) {
        ResultGraph<T> resultGraph = this.resultGraph;
        if (nodesToFetch != null) {
            EntityGraph<T> entityGraph = createEntityGraph(em, nodesToFetch);
            this.resultGraph = this.nodesToFetchOnly
                    ? ResultGraph.specifiedAssociationsOnly(entityGraph)
                    : ResultGraph.specifiedAndEagerAssociations(entityGraph);
        }
        if (resultGraph != null) {
            typedQuery.setHint(resultGraph.getType(), resultGraph.getEntityGraph());
        }
    }

    public Optional<T> get(EntityManager em) {
        List<T> resultList = this.createQuery(em).getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        if (resultList.size() > 1) {
            throw new NonUniqueResultException("More than one result found: " + resultList.size());
        }
        return Optional.of(resultList.get(0));
    }

    public T load(EntityManager em) {
        return this.createQuery(em).getSingleResult();
    }

    public List<T> find(EntityManager em) {
        return this.createQuery(em).getResultList();
    }

    private EntityGraph<T> createEntityGraph(EntityManager em, List<Attribute<T, ?>> nodesToAdd) {
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
