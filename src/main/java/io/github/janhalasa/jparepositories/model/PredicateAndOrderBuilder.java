package io.github.janhalasa.jparepositories.model;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

public interface PredicateAndOrderBuilder<T> {

    PredicateAndOrder build(CriteriaBuilder cb, Root<T> root);

    static <T> PredicateAndOrderBuilder<T> of(PredicateBuilder<T> predicateBuilder) {
        return (cb, root) -> new PredicateAndOrder(predicateBuilder.build(cb, root), null);
    }
}
