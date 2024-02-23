package io.github.janhalasa.jparepositories.model;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * One method interface meant to be used by la
 *
 * @author janhalasa
 */
public interface PredicateBuilder<T> {
	
	Predicate build(CriteriaBuilder cb, Root<T> root);
}
