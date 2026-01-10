package io.github.janhalasa.jparepositories.model;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.stream.Collectors;

public interface PredicateAndOrderBuilder<T> {

    PredicateAndOrder build(CriteriaBuilder cb, Root<T> root);

    static <T> PredicateAndOrderBuilder<T> of(PredicateBuilder<T> predicateBuilder) {
        return (cb, root) -> new PredicateAndOrder(predicateBuilder.build(cb, root), List.of());
    }

    default QueryBuilder<T> toQueryBuilder(boolean requireOrdering) {
        return (cb, root, query, omitSorting) -> {
            final PredicateAndOrder predicateAndOrder = this.build(cb, root);
            if (predicateAndOrder.getPredicate() != null) {
                query.where(predicateAndOrder.getPredicate());
            }
            if (! omitSorting) {
                if (requireOrdering && predicateAndOrder.getOrders() == null) {
                    throw new IllegalArgumentException("This query (probably pagination) requires a sort definition " +
                            "to be able to produce repeatable results.");
                }
                if (predicateAndOrder.getOrders() != null) {
                    query.orderBy(buildOrderBy(predicateAndOrder.getOrders(), cb));
                }
            }
        };
    }

    private static List<Order> buildOrderBy(List<OrderBy> orderByList, CriteriaBuilder cb) {
        return orderByList.stream()
                .map(orderBy -> orderBy.toJpa(cb))
                .collect(Collectors.toList());
    }
}
