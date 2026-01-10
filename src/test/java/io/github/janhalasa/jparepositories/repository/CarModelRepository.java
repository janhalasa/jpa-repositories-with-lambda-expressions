package io.github.janhalasa.jparepositories.repository;

import io.github.janhalasa.jparepositories.CrudRepository;
import io.github.janhalasa.jparepositories.entity.Car;
import io.github.janhalasa.jparepositories.entity.CarModel;
import io.github.janhalasa.jparepositories.entity.CarModel_;
import io.github.janhalasa.jparepositories.entity.Car_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;

import java.util.List;

/**
 *
 * @author janhalasa
 */
public class CarModelRepository extends CrudRepository<CarModel, Long> {
	
	public CarModelRepository(EntityManager em) {
		super(em, CarModel.class, CarModel_.id);
	}

	public List<CarModel> findWithSelectByCarColors(String color1, String color2, boolean distinct) {
		return select()
				.where((cb, root) -> {
					Join<CarModel, Car> carsJoin = root.join(CarModel_.cars);
					return cb.or(
							cb.equal(carsJoin.get(Car_.color), color1),
							cb.equal(carsJoin.get(Car_.color), color2)
					);
				})
				.distinct(distinct)
				.find(this.em());
	}
}
