package com.halasa.criterialambda.repository;

import com.halasa.criterialambda.CrudRepository;
import com.halasa.criterialambda.entity.CarModel;
import com.halasa.criterialambda.entity.CarModel_;

import javax.persistence.EntityManager;

/**
 *
 * @author janhalasa
 */
public class CarModelRepository extends CrudRepository<CarModel, Long> {
	
	public CarModelRepository(EntityManager em) {
		super(em, CarModelRepository.class, CarModel.class, CarModel_.id);
	}
}
