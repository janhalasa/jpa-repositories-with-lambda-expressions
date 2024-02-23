package io.github.janhalasa.jparepositories.model;

import jakarta.persistence.EntityGraph;

public class ResultGraph<T> {

    private static final String JAKARTA_PERSISTENCE_LOADGRAPH = "jakarta.persistence.loadgraph";
    private static final String JAKARTA_PERSISTENCE_FETCHGRAPH = "jakarta.persistence.fetchgraph";

    private final EntityGraph<T> entityGraph;
    private final String type;

    public ResultGraph(EntityGraph<T> entityGraph, String type) {
        this.entityGraph = entityGraph;
        this.type = type;
    }

    public static <T> ResultGraph<T> specifiedAssociationsOnly(EntityGraph<T> entityGraph) {
        return new ResultGraph<T>(entityGraph, JAKARTA_PERSISTENCE_FETCHGRAPH);
    }

    public static <T> ResultGraph<T> specifiedAndEagerAssociations(EntityGraph<T> entityGraph) {
        return new ResultGraph<T>(entityGraph, JAKARTA_PERSISTENCE_LOADGRAPH);
    }

    public EntityGraph<T> getEntityGraph() {
        return entityGraph;
    }

    public String getType() {
        return type;
    }
}
