package io.github.janhalasa.jparepositories.model;

/**
 * Rozhranie pre entity, ktoré sa dajú meniť a podporujú version atribút pre optimistic locking.
 */
public interface VersionAware {

    Integer getVersion();
}
