package io.github.janhalasa.jparepositories.model;

import jakarta.persistence.criteria.Predicate;

import java.util.List;

public class PredicateAndOrder {

    private final Predicate predicate;
    private final List<OrderBy> orders;

    public PredicateAndOrder(Predicate predicate, List<OrderBy> orders) {
        this.predicate = predicate;
        this.orders = orders;
    }

    public PredicateAndOrder(Predicate predicate, OrderBy orderBy) {
        this.predicate = predicate;
        this.orders = List.of(orderBy);
    }

    public PredicateAndOrder(List<OrderBy> orders) {
        this(null, orders);
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public List<OrderBy> getOrders() {
        return orders;
    }
}
