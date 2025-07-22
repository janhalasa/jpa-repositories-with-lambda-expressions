package io.github.janhalasa.jparepositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.transaction.Transactional;

public abstract class ReadPersistRepository<T, P> extends ReadOnlyRepository<T, P> {

    /**
     * This no-param constructor is here just for CDI. In your code, use the one with parameters.
     */
    @Deprecated
    protected ReadPersistRepository() {
    }

    protected ReadPersistRepository(
            EntityManager em,
            Class<T> entityClass,
            SingularAttribute<T, P> pkField) {
        super(em, entityClass, pkField);
    }

    @Transactional
    public void persist(T entity) {
        super.persist(entity);
    }
}
