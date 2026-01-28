package io.github.janhalasa.jparepositories.model;

import jakarta.persistence.criteria.Root;

public interface Fetcher<T> {

    void create(Root<T> root);
}
