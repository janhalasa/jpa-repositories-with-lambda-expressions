package io.github.janhalasa.jparepositories.model;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * A utility class for constructing ordering attributes used in query definitions.
 * The class allows specifying an attribute and its sort direction (ascending or descending).
 * In contrast to OrderBy class, OrderAttr does not require its Root object.
 *
 * @param <T> the type of the entity that the attribute belongs to
 */
public class OrderAttr<T> {

    private final OrderBy.OrderDirection direction;
    private final SingularAttribute<T, ?> attribute;

    private OrderAttr(SingularAttribute<T, ?> attribute, OrderBy.OrderDirection direction) {
        this.direction = direction;
        this.attribute = attribute;
    }

    public static <E> OrderAttr<E> of(SingularAttribute<E, ?> attribute, boolean ascending) {
        return new OrderAttr<>(
                attribute,
                ascending ? OrderBy.OrderDirection.ASC : OrderBy.OrderDirection.DESC);
    }

    public static <E> OrderAttr<E> asc(SingularAttribute<E, ?> attribute) {
        return new OrderAttr<>(
                attribute,
                OrderBy.OrderDirection.ASC);
    }

    public static <E> OrderAttr<E> desc(SingularAttribute<E, ?> attribute) {
        return new OrderAttr<>(
                attribute,
                OrderBy.OrderDirection.DESC);
    }

    public OrderBy.OrderDirection getDirection() {
        return direction;
    }

    public SingularAttribute<?, ?> getAttribute() {
        return attribute;
    }

    public OrderBy toOrderBy(Root<T> root) {
        return OrderBy.of(root.get(this.attribute), this.direction == OrderBy.OrderDirection.ASC);
    }
}
