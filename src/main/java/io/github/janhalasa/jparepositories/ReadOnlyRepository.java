package io.github.janhalasa.jparepositories;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Optional;

public class ReadOnlyRepository<T, P> extends BasicRepository<T, P> {

    /**
     * This no-param constructor is here just for CDI. In your code, use the one with parameters.
     */
    @Deprecated
    protected ReadOnlyRepository() {
    }

    protected ReadOnlyRepository(
            EntityManager em,
            Class<T> entityClass,
            SingularAttribute<T, P> pkField) {
        super(em, entityClass, pkField);
    }

    public Optional<T> getByPk(P pkValue) {
        return super.getByPk(pkValue);
    }

    public Optional<T> getByPk(P pkValue, EntityGraph<T> entityLoadGraph) {
        return super.getByPk(pkValue, entityLoadGraph);
    }

    public T loadByPk(P pkValue) {
        return super.loadByPk(pkValue);
    }

    public T loadByPk(P pkValue, EntityGraph<T> entityLoadGraph) {
        return super.loadByPk(pkValue, entityLoadGraph);
    }

    public List<T> findAll() {
        return super.findAll();
    }

    public List<T> findAll(EntityGraph<T> entityLoadGraph) {
        return super.findAll(entityLoadGraph);
    }
}
