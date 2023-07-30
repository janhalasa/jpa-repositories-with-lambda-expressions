package io.github.janhalasa.jparepositories;

import io.github.janhalasa.jparepositories.entity.Car;
import io.github.janhalasa.jparepositories.entity.CarModel;
import io.github.janhalasa.jparepositories.entity.Vendor;
import io.github.janhalasa.jparepositories.repository.CarRepository;
import io.github.janhalasa.jparepositories.repository.VendorRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnitUtil;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author janhalasa
 */
public class RepositoryTest {
	
	private static final Logger LOGGER = Logger.getLogger(RepositoryTest.class.getName());
	private static final String RENAULT = "Renault";

	private EntityManager em;
	private PersistenceUnitUtil unitUtil;

	private VendorRepository vendorRepository;
	private CarRepository carRepository;

	@BeforeEach
	void beforeEach() {
		if (this.em == null) {
			EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
			this.em = entityManagerFactory.createEntityManager();
			this.unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();

			this.vendorRepository = new VendorRepository(this.em);
			this.carRepository = new CarRepository(this.em);
		}
	}

	@Test
	void testPaginationAndLoadGraph() {
		rollback(() -> {
			final int pageNumber = 1;
			final int pageSize = 7;

			Vendor vendor = vendorRepository.loadByName(RENAULT);
			Assertions.assertFalse(unitUtil.isLoaded(vendor.getModels()));
			List<CarModel> models = vendor.getModels();
			Assertions.assertEquals(3, models.size());

			final ResultPage<Vendor> vendorResultPage = vendorRepository.pageWhereNameContainsA(pageNumber, pageSize);
			Assertions.assertEquals(11, vendorResultPage.getTotalCount());
			Assertions.assertEquals(pageNumber, vendorResultPage.getPageNumber());
			Assertions.assertEquals(pageSize, vendorResultPage.getPageSize());
			Assertions.assertEquals(pageSize, vendorResultPage.getResults().size());

			vendorResultPage.getResults().forEach(vendorFromPage -> {
				Assertions.assertTrue(unitUtil.isLoaded(vendorFromPage.getModels()));
			});
		});
	}

	@Test
	void testVersionAwareLoad() {
		rollback(() -> {
			Vendor renault = vendorRepository.loadByName(RENAULT);
			Assertions.assertThrows(
					OptimisticLockException.class,
					() -> vendorRepository.loadByPk(renault.getId(), renault.getVersion() + 1));
		});
	}

	@Test
	void testFindWhereAndOrderByWithLoadGraph() {
		rollback(() -> {
			List<Vendor> vendors = vendorRepository.findLikeNameOrderByNameAndFetchModels("e");
			Assertions.assertEquals(
					List.of("Mercedes", "Peugeot", "Renault", "Seat", "Tesla", "Volkswagen"),
					vendors.stream().map(Vendor::getName).collect(Collectors.toList()));
			vendors.forEach(vendor -> Assertions.assertTrue(unitUtil.isLoaded(vendor.getModels())));
		});
	}

	@Test
	void testFindWithMultiplePredicates() {
		rollback(() -> {
			Vendor renault = this.vendorRepository.loadByName(RENAULT);
			List<Car> cars = this.carRepository.findByVendorAndColor(renault, "green");
			Assertions.assertEquals(3, cars.size());
		});
	}

	@Test
	void testLoadFailsOnNonUniqueResult() {
		rollback(() -> {
			Assertions.assertThrows(
					NonUniqueResultException.class,
					() -> this.carRepository.loadByColor("green"));
		});
	}

	@Test
	void testLoadReturnsValueOnUniqueResult() {
		rollback(() -> {
			String color = "red";
			Car car = this.carRepository.loadByColor(color);
			Assertions.assertEquals(
					color,
					car.getColor());
		});
	}

	@Test
	void testGetFailsOnNonUniqueResult() {
		rollback(() -> Assertions.assertThrows(
				NonUniqueResultException.class,
				() -> RepositoryTest.this.carRepository.getByColor("green")));
	}

	@Test
	void testGetReturnsEmptyWhenNotFound() {
		rollback(() -> Assertions.assertEquals(
				Optional.empty(),
				this.carRepository.getByColor("yellowgreen")));
	}

	@Test
	void testGetReturnsValueOnUniqueResult() {
		rollback(() -> {
			String color = "red";
			Car car = this.carRepository.getByColor(color).get();
			Assertions.assertEquals(
					color,
					car.getColor());
		});
	}

	private void rollback(Runnable runnable) {
		em.getTransaction().begin();
		try {
			runnable.run();
		} finally {
			em.getTransaction().rollback();
		}
	}
}
