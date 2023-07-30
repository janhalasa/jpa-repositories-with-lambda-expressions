package com.halasa.criterialambda;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;

public abstract class ReadPersistRepository<T, P> extends ReadOnlyRepository<T, P> {

    /**
     * This no-param constructor is here just for CDI. In your code, use the one with parameters.
     */
    @Deprecated
    protected ReadPersistRepository() {
    }

    protected ReadPersistRepository(
            EntityManager em,
            Class<? extends ReadPersistRepository<T, P>> repositoryClass,
            Class<T> entityClass,
            SingularAttribute<T, P> pkField) {
        super(em, repositoryClass, entityClass, pkField);
    }

    public void persist(T entity) {
        super.persist(entity);
    }
}
