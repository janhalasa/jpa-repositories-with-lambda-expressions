package io.github.janhalasa.jparepositories.model;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 *
 * @author jan
 */
public interface QueryBuilder<T> {

	void build(CriteriaBuilder criteriaBuilder, Root<T> root, CriteriaQuery<?> criteriaQuery, boolean omitSorting);
}
