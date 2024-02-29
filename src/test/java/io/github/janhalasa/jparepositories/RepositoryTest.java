package io.github.janhalasa.jparepositories;

import io.github.janhalasa.jparepositories.entity.Car;
import io.github.janhalasa.jparepositories.entity.CarModel;
import io.github.janhalasa.jparepositories.entity.Vendor;
import io.github.janhalasa.jparepositories.repository.CarRepository;
import io.github.janhalasa.jparepositories.repository.VendorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author janhalasa
 */
public class RepositoryTest {

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
			assertFalse(unitUtil.isLoaded(vendor.getModels()));
			Set<CarModel> models = vendor.getModels();
			assertEquals(3, models.size());

			final ResultPage<Vendor> vendorResultPage = vendorRepository.pageWhereNameContainsA(pageNumber, pageSize);
			assertEquals(11, vendorResultPage.getTotalCount());
			assertEquals(pageNumber, vendorResultPage.getPageNumber());
			assertEquals(pageSize, vendorResultPage.getPageSize());
			assertEquals(pageSize, vendorResultPage.getResults().size());

			vendorResultPage.getResults().forEach(vendorFromPage ->
				assertTrue(unitUtil.isLoaded(vendorFromPage.getModels()))
			);
		});
	}

	@Test
	void fetchGraphIgnoresEagerAssociations() {
		Vendor vendor = this.vendorRepository.loadWithModelsAndWithoutManufacturingPlants(1000L);
		assertTrue(unitUtil.isLoaded(vendor.getModels()));
		assertFalse(unitUtil.isLoaded(vendor.getManufacturingPlants()));
	}

	@Test
	void loadGraphIncludesEagerAssociations() {
		Vendor vendor = this.vendorRepository.loadWithModelsAndManufacturingPlants(1000L);
		assertTrue(unitUtil.isLoaded(vendor.getModels()));
		assertTrue(unitUtil.isLoaded(vendor.getManufacturingPlants()));
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
			assertEquals(
					List.of("Mercedes", "Peugeot", "Renault", "Seat", "Tesla", "Volkswagen"),
					vendors.stream().map(Vendor::getName).collect(Collectors.toList()));
			vendors.forEach(vendor -> assertTrue(unitUtil.isLoaded(vendor.getModels())));
		});
	}

	@Test
	void testFindWithMultiplePredicates() {
		rollback(() -> {
			Vendor renault = this.vendorRepository.loadByName(RENAULT);
			List<Car> cars = this.carRepository.findByVendorAndColor(renault, "green");
			assertEquals(3, cars.size());
		});
	}

	@Test
	void testLoadFailsOnNonUniqueResult() {
		rollback(() ->
			Assertions.assertThrows(
					NonUniqueResultException.class,
					() -> this.carRepository.loadByColor("green"))
		);
	}

	@Test
	void testLoadReturnsValueOnUniqueResult() {
		rollback(() -> {
			String color = "red";
			Car car = this.carRepository.loadByColor(color);
			assertEquals(
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
		rollback(() -> assertEquals(
				Optional.empty(),
				this.carRepository.getByColor("yellowgreen")));
	}

	@Test
	void testGetReturnsValueOnUniqueResult() {
		rollback(() -> {
			String color = "red";
			Car car = this.carRepository.getByColor(color).get();
			assertEquals(
					color,
					car.getColor());
		});
	}

	@Test
	void givenQueryWithJoins_whenPageWhere_thenCorrectCountCalculated() {
		int pageNumber = 1;
		int pageSize = 100;
		int expectedTotalCount = 1;
		ResultPage<Vendor> vendorResultPage = this.vendorRepository.pageWhereModelNameContainsA(pageNumber, pageSize);
		assertEquals(pageNumber, vendorResultPage.getPageNumber());
		assertEquals(pageSize, vendorResultPage.getPageSize());
		assertEquals(expectedTotalCount, vendorResultPage.getTotalCount());
		assertEquals(expectedTotalCount, vendorResultPage.getResults().size());
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
