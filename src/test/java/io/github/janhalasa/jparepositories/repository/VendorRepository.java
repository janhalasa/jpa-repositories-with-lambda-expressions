package io.github.janhalasa.jparepositories.repository;

import io.github.janhalasa.jparepositories.ResultPage;
import io.github.janhalasa.jparepositories.VersionAwareCrudRepository;
import io.github.janhalasa.jparepositories.entity.CarModel;
import io.github.janhalasa.jparepositories.entity.CarModel_;
import io.github.janhalasa.jparepositories.entity.Vendor;
import io.github.janhalasa.jparepositories.entity.Vendor_;
import io.github.janhalasa.jparepositories.model.OrderBy;
import io.github.janhalasa.jparepositories.model.PredicateAndOrder;
import io.github.janhalasa.jparepositories.model.ResultGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.SetJoin;

import java.util.List;

/**
 *
 * @author janhalasa
 */
public class VendorRepository extends VersionAwareCrudRepository<Vendor, Long> {
	
	public VendorRepository(EntityManager em) {
		super(em, Vendor.class, Vendor_.id);
	}

	public ResultPage<Vendor> pageWhereNameContainsA(int pageNumber, int pageSize) {
		return super.pageWhere(
				(cb, root) -> new PredicateAndOrder(
					cb.like(root.get(Vendor_.name), "%a%"),
					List.of(OrderBy.asc(
							root.get(Vendor_.name)),
							OrderBy.desc(root.get(Vendor_.id))
					)
				),
				pageNumber,
				pageSize,
				true,
				ResultGraph.specifiedAssociationsOnly(createEntityGraph(List.of(Vendor_.models))));
	}

	public ResultPage<Vendor> pageWhereModelNameContainsA(int pageNumber, int pageSize) {
		return super.pageWhere(
				(cb, root) -> {
					final SetJoin<Vendor, CarModel> carModelJoin = root.join(Vendor_.models, JoinType.INNER);

					return new PredicateAndOrder(
							cb.like(carModelJoin.get(CarModel_.name), "%a%"),
							List.of(OrderBy.asc(
											root.get(Vendor_.name)),
									OrderBy.desc(root.get(Vendor_.id))
							)
					);
				},
				pageNumber,
				pageSize,
				true,
				ResultGraph.specifiedAssociationsOnly(createEntityGraph(List.of(Vendor_.models))));
	}

	public Vendor loadByName(String name) {
		return loadWhere((cb, root) -> cb.equal(root.get(Vendor_.name), name));
	}

	public List<Vendor> findLikeNameOrderByNameAndFetchModels(String namePattern) {
		return findWhereOrdered(
				(cb, root) -> new PredicateAndOrder(
					cb.like(root.get(Vendor_.name), "%" + namePattern + "%"),
					List.of(OrderBy.asc(root.get(Vendor_.name))
				)), ResultGraph.specifiedAndEagerAssociations(createEntityGraph(List.of(Vendor_.models))));
	}

	public Vendor loadWithModelsAndWithoutManufacturingPlants(Long vendorId) {
		return loadByPk(vendorId, ResultGraph.specifiedAssociationsOnly(createEntityGraph(List.of(Vendor_.models))));
	}

	public Vendor loadWithModelsAndManufacturingPlants(Long vendorId) {
		return loadByPk(vendorId, ResultGraph.specifiedAndEagerAssociations(createEntityGraph(List.of(Vendor_.models))));
	}
}
