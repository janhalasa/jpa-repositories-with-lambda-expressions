package io.github.janhalasa.jparepositories.repository;

import io.github.janhalasa.jparepositories.CrudRepository;
import io.github.janhalasa.jparepositories.entity.CarModel;
import io.github.janhalasa.jparepositories.entity.CarModel_;
import jakarta.persistence.EntityManager;

/**
 *
 * @author janhalasa
 */
public class CarModelRepository extends CrudRepository<CarModel, Long> {
	
	public CarModelRepository(EntityManager em) {
		super(em, CarModel.class, CarModel_.id);
	}
}
