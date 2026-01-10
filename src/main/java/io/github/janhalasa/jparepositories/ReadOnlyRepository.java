package io.github.janhalasa.jparepositories;

import io.github.janhalasa.jparepositories.model.ResultGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.SingularAttribute;

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
            SingularAttribute<? super T, P> pkField) {
        super(em, entityClass, pkField);
    }

    public Optional<T> getByPk(P pkValue) {
        return super.getByPk(pkValue);
    }

    public Optional<T> getByPk(P pkValue, ResultGraph<T> resultGraph) {
        return super.getByPk(pkValue, resultGraph);
    }

    public T loadByPk(P pkValue) {
        return super.loadByPk(pkValue);
    }

    public T loadByPk(P pkValue, ResultGraph<T> resultGraph) {
        return super.loadByPk(pkValue, resultGraph);
    }

    public List<T> findAll() {
        return super.findAll();
    }

    public List<T> findAll(ResultGraph<T> resultGraph) {
        return super.findAll(resultGraph);
    }
}
