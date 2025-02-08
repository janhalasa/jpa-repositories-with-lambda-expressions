package io.github.janhalasa.jparepositories.model;

public class PagingParams<T> {

    private final QueryParams<T> queryParams;
    private final int pageNumber;
    private final int pageSize;

    public PagingParams(
            QueryBuilder<T> queryBuilder,
            int pageNumber,
            int pageSize,
            ResultGraph<T> resultGraph,
            boolean distinct) {

        if (queryBuilder == null) {
            throw new IllegalArgumentException("Pagination requires predicateAndOrderBuilder or queryBuilder" +
                    " - at least a sort definition.");
        }

        this.queryParams = new QueryParams<>(
                queryBuilder,
                resultGraph,
                distinct);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public PagingParams(
            PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
            int pageNumber,
            int pageSize,
            ResultGraph<T> resultGraph,
            boolean distinct) {
        this(
                predicateAndOrderBuilder == null ? null : predicateAndOrderBuilder.toQueryBuilder(true),
                pageNumber,
                pageSize,
                resultGraph,
                distinct);
    }

    public PagingParams(
            PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
            int pageNumber,
            int pageSize,
            ResultGraph<T> resultGraph) {
        this(predicateAndOrderBuilder, pageNumber, pageSize, resultGraph, false);
    }

    public PagingParams(
            PredicateAndOrderBuilder<T> predicateAndOrderBuilder,
            int pageNumber,
            int pageSize) {
        this(predicateAndOrderBuilder, pageNumber, pageSize, null, false);
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public QueryParams<T> getQueryParams() {
        return queryParams;
    }
}
