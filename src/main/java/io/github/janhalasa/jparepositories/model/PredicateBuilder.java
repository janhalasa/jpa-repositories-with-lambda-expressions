package io.github.janhalasa.jparepositories.model;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * One method interface meant to be used by la
 *
 * @author janhalasa
 */
public interface PredicateBuilder<T> {
	
	Predicate build(CriteriaBuilder cb, Root<T> root);
}
