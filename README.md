# JPA repositories with lambda expressions #

Lambda expression support for JPA (Java persistence API) Criteria API repositories.

Basic Criteria API is hard to write, because its calls cannot be chained. This library solves the problem. 
The resulting code is short, type safe and easy to read.

## Usage ##

Add the library as a Maven dependency:

```xml
<dependency>
    <artifactId>jpa-repositories-with-lambda-expressions</artifactId>
    <groupId>io.github.janhalasa</groupId>
    <version>0.7.3</version>
</dependency>
```
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