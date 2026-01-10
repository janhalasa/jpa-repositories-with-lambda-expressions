package io.github.janhalasa.jparepositories;

import io.github.janhalasa.jparepositories.model.PredicateBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.transaction.Transactional;

public class CrudRepository<T, P> extends ReadPersistRepository<T, P> {

    /**
     * This no-param constructor is here just for CDI. In your code, use the one with parameters.
     */
    @Deprecated
    protected CrudRepository() {
    }

    protected CrudRepository(
            EntityManager em,
            Class<T> entityClass,
            SingularAttribute<? super T, P> pkField) {
        super(em, entityClass, pkField);
    }

    @Transactional
    public T merge(T entity) {
        return super.merge(entity);
    }

    @Transactional
    public void remove(T entity) {
        super.remove(entity);
    }

    @Transactional
    public void removeByPk(P entityPk) {
        super.removeByPk(entityPk);
    }

    protected int removeWhere(PredicateBuilder<T> predicateBuilder) {
        return super.removeWhere(predicateBuilder);
    }
}
