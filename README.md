# JPA repositories with lambda expressions #

Lambda expression support for JPA (Java persistence API) Criteria API repositories.

Basic Criteria API is hard to write, because its calls cannot be chained. This library solves the problem. 
The resulting code is short, type safe and easy to read.

If you like this library, please give it a star, so I know someone uses it.

## Usage ##

Add the library as a Maven dependency:

```xml
<dependency>
    <artifactId>jpa-repositories-with-lambda-expressions</artifactId>
    <groupId>io.github.janhalasa</groupId>
    <version>0.9.2</version>
</dependency>
```

## Fluent query API ##

The `io.github.janhalasa.jparepositories.select.Select` class provides a fluent API for building database queries.
For constructing a where clause, it uses basic JPA Criteria API, but provides the CriteriaBuilder and Root objects,
so it's straightforward.

This API can be used from the repositories described below using the `select()` method.

Example:
```java
public List<Car> findCarsByBrand(String brandName) {
    Select.from(Car.class, entityManager)
            .where((cb, root) -> cb.equal(root.get(Car_.brand), brandName))
            .orderBy(OrderAttr.asc(Car_.vin))
            .distinct()
            .list();
}
```

Useful API methods:
* `Optional<T> optional()` For uncertain searches of a single record.
* `T single()` Returns a single record or throws an Exception if none or more than one found.
* `List<T> list()` Returns all records found.
* `ResultPage<T> page(int pageNumber, int pageSize)` Returns the requested page of results and the total count
* `long count()` Returns number of records matching given criteria.

### Fetching associations
Fetching of associations in JPA criteria API requires access to the `Root` object.
The `Select` class allows few ways how to do it:
* Using an entity graph with the `Select::fetch(ResultGraph<T> resultGraph)` method. To create a graph, one needs an `EntityManager`, it's API is hard to read, and it's not possible to define a graph deeper than 2.
* Using the `Fetcher` class with `Select::fetch(Fetcher fetcher)` method. `Fetcher` is a functional interface. It provides `Root` object as a parameter, which can be used to fetch anything. It's easy to reuse it, and it doesn't need `EntityManager` to be created.
* Using simple attributes of the root entity with methods `fetchOnly(List<Attribute<T, ?>> nodesToFetch)` and `fetchExtra(List<Attribute<T, ?>> nodesToFetch)`. This way is easy to use, but allows fetching only direct associations of the root entity.

## Repositories ##

To create a repository class, extend one of the provided repositories:

* `BasicRepository` - no public methods - just methods you can use to create your custom repository
* `ReadOnlyRepository` - extends `BasicRepository` and makes some querying methods public.
* `ReadPersistRepository` - extends `ReadPersistRepository` and makes the `persist` method public. Intended for repositories that don't support updating or removing of entities.
* `CrudRepository` - extends `ReadPersistRepository` and makes `merge` nad `remove` methods public.
* `VersionAwareCrudRepository` - extends `CrudRepository` and adds load methods with an extra `expectedVersion` parameter.

Example:
```java
public class CarRepository extends CrudRepository<Car, Long> {
    public CarRepository(EntityManager em) {
        super(em, Car.class, Car_.id);
    }
    // ... custom repository methods
}
```

## Querying ##

### Single ####

#### Optional result ####

 ```java
Optional<E> optionalEntity = getWhere((cb, root) -> cb.equal(root.get(Car_.color), color));
```

#### Required result ####

If no result is found, `jakarta.persistence.NoResultException` is thrown.

```java
E Entity = loadWhere((cb, root) -> cb.equal(root.get(Car_.color), color));
```

### Multiple ###

#### Find a list ####

Methods starting with find. They always return a list of entities.

```java
List<E> entities = findWhere(
    (cb, root) -> cb.and(
        cb.equal(root.join(Car_.model).get(CarModel_.vendor), vendor),
        cb.equal(root.get(Car_.color), color))
);
```

#### Ordering ####

Ordering is supported by the find methods using `OrderBy` class which doesn't depend on `EntityManager`:

```java
List<E> entities =  findWhereOrdered(
    (cb, root) -> new PredicateAndOrder(
        cb.like(root.get(Vendor_.name), "%" + namePattern + "%"),
        List.of(OrderBy.asc(root.get(Vendor_.name))))
);
```

#### Pagination ####

Pagination requires an order (a stable one) to work and page properties - number and size:

```java
ResultPage<E> page = pageWhere(
    (cb, root) -> new PredicateAndOrder(
        cb.like(root.get(Vendor_.name), "%a%"),
        List.of(OrderBy.asc(
                root.get(Vendor_.name)),
                OrderBy.desc(root.get(Vendor_.id))
        )
    ),
    pageNumber,
    pageSize);
```

There are no public pagination methods exposed in any of the predefined repository classes, 
since pagination requires a custom order. The primary key could be used by default (it would also ensure a stable order),
but this functionality is not there at the moment.

### Lazy assotiation loading ###

The API supports lazy assotiation loading using entity graphs. Both `jakarta.persistence.loadgraph` and `jakarta.persistence.fetchgraph`)
are supported by methods with more explicit naming `ResultGraph.specifiedAssociationsOnly` and `ResultGraph.specifiedAndEagerAssociations`.

```java
findWhere(
        (cb, root) -> cb.equal(root.get(Vendor_.name), searchedName),
        ResultGraph.specifiedAssociationsOnly(createEntityGraph(List.of(Vendor_.models))));
```

### Fluent API ###

The repositories support the fluent API described at the beginning of this document. Use the `select()` method,
which replaces the `Select.from(class, em)`, because the repositories already have those parameter values.

## Modifications ##

`BasicRepository` supports basic persist, merge and remove methods analogous to the ones from the JPA `EntityManager`.
It also supports removal of multiple entities by `BasicRepository::removeWhere`.

```java
removeWhere((cb, root) -> cb.equal(root.get(Car_.color), color));
```

### Versioning and optimistic locking ###

The library supports optimistic locking by a possibility to load an entity by its primary key and a version.
The intended use case is: A service gets a primary key and a version from and tries to load the corresponding entity. 
If the version of the entity has changed the `VersionAwareCrudRepository::loadByPk(pk, expectedVersion)` will throw
an `jakarta.persistence.OptimisticLockException`.

```java
E entity = loadByPk(pk, expectedVersion);
// ... modify entity
// modifications will be saved on transaction commit
```