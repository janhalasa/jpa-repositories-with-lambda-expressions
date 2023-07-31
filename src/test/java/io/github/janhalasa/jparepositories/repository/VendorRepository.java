package io.github.janhalasa.jparepositories.repository;

import io.github.janhalasa.jparepositories.ResultPage;
import io.github.janhalasa.jparepositories.VersionAwareCrudRepository;
import io.github.janhalasa.jparepositories.entity.Vendor;
import io.github.janhalasa.jparepositories.entity.Vendor_;
import io.github.janhalasa.jparepositories.model.OrderBy;
import io.github.janhalasa.jparepositories.model.PredicateAndOrder;

import javax.persistence.EntityManager;
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
				createEntityGraph(List.of(Vendor_.models)));
	}

	public Vendor loadByName(String name) {
		return loadWhere((cb, root) -> cb.equal(root.get(Vendor_.name), name));
	}

	public List<Vendor> findLikeNameOrderByNameAndFetchModels(String namePattern) {
		return findWhereOrdered(
				(cb, root) -> new PredicateAndOrder(
					cb.like(root.get(Vendor_.name), "%" + namePattern + "%"),
					List.of(OrderBy.asc(root.get(Vendor_.name))
				)), createEntityGraph(List.of(Vendor_.models)));
	}
}
