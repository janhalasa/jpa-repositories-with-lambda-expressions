package io.github.janhalasa.jparepositories;

import io.github.janhalasa.jparepositories.model.ResultGraph;
import io.github.janhalasa.jparepositories.model.VersionAware;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Objects;

public abstract class VersionAwareCrudRepository<T extends VersionAware, P> extends ReadPersistRepository<T, P> {

    /**
     * This no-param constructor is here just for CDI. In your code, use the one with parameters.
     */
    @Deprecated
    protected VersionAwareCrudRepository() {
    }

    protected VersionAwareCrudRepository(
            EntityManager em,
            Class<T> entityClass,
            SingularAttribute<T, P> pkField) {
        super(em, entityClass, pkField);
    }

    public T loadByPk(P pk, Integer expectedVersion) {
        return this.loadByPkAndVersion(pk, expectedVersion, null);
    }

    public T loadByPk(P pk, Integer expectedVersion, ResultGraph<T> resultGraph) {
        return this.loadByPkAndVersion(pk, expectedVersion, resultGraph);
    }

    private T loadByPkAndVersion(P pk, Integer expectedVersion, ResultGraph<T> resultGraph) {
        Objects.requireNonNull(pk);
        Objects.requireNonNull(expectedVersion);
        final T entity = resultGraph == null
                ? this.loadByPk(pk)
                : this.loadByPk(pk, resultGraph);
        if (!Objects.equals(entity.getVersion(), expectedVersion)) {
            throw new OptimisticLockException("Required entity has been modified in a different transaction " +
                    "(expected version: " + expectedVersion + ", actual version: " + entity.getVersion() + ")");

        }
        return entity;
    }
}
