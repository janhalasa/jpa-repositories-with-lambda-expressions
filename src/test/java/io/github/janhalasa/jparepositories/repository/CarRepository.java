package io.github.janhalasa.jparepositories.repository;

import io.github.janhalasa.jparepositories.ReadPersistRepository;
import io.github.janhalasa.jparepositories.entity.Car;
import io.github.janhalasa.jparepositories.entity.CarModel_;
import io.github.janhalasa.jparepositories.entity.Car_;
import io.github.janhalasa.jparepositories.entity.Vendor;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author janhalasa
 */
public class CarRepository extends ReadPersistRepository<Car, Long> {
	
	public CarRepository(EntityManager em) {
		super(em, Car.class, Car_.id);
	}
	
	public List<Car> findByVendorAndColor(Vendor vendor, String color) {
		return findWhere(
			(cb, root) -> cb.and(
				cb.equal(root.join(Car_.model).get(CarModel_.vendor), vendor),
				cb.equal(root.get(Car_.color), color)
			)
		);
	}

	public Car loadByColor(String color) {
		return loadWhere((cb, root) -> cb.equal(root.get(Car_.color), color));
	}

	public Optional<Car> getByColor(String color) {
		return getWhere((cb, root) -> cb.equal(root.get(Car_.color), color));
	}

	public int removeByColor(String color) {
		return removeWhere((cb, root) -> cb.equal(root.get(Car_.color), color));
	}
}
