package io.github.janhalasa.jparepositories.model;

public class QueryParams<T> {

    private final QueryBuilder<T> queryBuilder;
    private final ResultGraph<T> resultGraph;
    private final boolean distinct;

    public QueryParams(QueryBuilder<T> queryBuilder, ResultGraph<T> resultGraph, boolean distinct) {
        this.queryBuilder = queryBuilder;
        this.distinct = distinct;
        this.resultGraph = resultGraph;
    }

    public QueryParams(PredicateAndOrderBuilder<T> predicateAndOrderBuilder, ResultGraph<T> resultGraph, boolean distinct) {
        this(
                predicateAndOrderBuilder == null ? null : predicateAndOrderBuilder.toQueryBuilder(false),
                resultGraph,
                distinct);
    }

    public QueryParams(PredicateAndOrderBuilder<T> predicateAndOrderBuilder, ResultGraph<T> resultGraph) {
        this(predicateAndOrderBuilder, resultGraph, false);
    }

    public QueryParams(PredicateAndOrderBuilder<T> predicateAndOrderBuilder) {
        this(predicateAndOrderBuilder, null, false);
    }

    public QueryParams(PredicateBuilder<T> predicateBuilder, ResultGraph<T> resultGraph, boolean distinct) {
        this(PredicateAndOrderBuilder.of(predicateBuilder), resultGraph, distinct);
    }

    public QueryParams(PredicateBuilder<T> predicateBuilder, ResultGraph<T> resultGraph) {
        this(PredicateAndOrderBuilder.of(predicateBuilder), resultGraph, false);
    }

    public QueryParams(PredicateBuilder<T> predicateBuilder) {
        this(PredicateAndOrderBuilder.of(predicateBuilder), null, false);
    }

    public boolean isDistinct() {
        return distinct;
    }

    public ResultGraph<T> getResultGraph() {
        return resultGraph;
    }

    public QueryBuilder<T> getQueryBuilder() {
        return queryBuilder;
    }
}
