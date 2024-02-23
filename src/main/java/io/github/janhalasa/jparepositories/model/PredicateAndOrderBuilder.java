package io.github.janhalasa.jparepositories.model;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;

public interface PredicateAndOrderBuilder<T> {

    PredicateAndOrder build(CriteriaBuilder cb, Root<T> root);

    static <T> PredicateAndOrderBuilder<T> of(PredicateBuilder<T> predicateBuilder) {
        return (cb, root) -> new PredicateAndOrder(predicateBuilder.build(cb, root), null);
    }
}
