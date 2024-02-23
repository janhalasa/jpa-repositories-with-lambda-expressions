package io.github.janhalasa.jparepositories.entity;

import io.github.janhalasa.jparepositories.model.VersionAware;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;

import java.util.Set;

/**
 *
 * @author janhalasa
 */
@Entity
public class Vendor implements VersionAware {

	@Id
	@GeneratedValue
	private Long id;

	private String name;

	@Version
	private Integer version;

	@OneToMany()
	@JoinColumn(name="vendor_id", nullable=true)
	private Set<CarModel> models;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name="vendor_id")
	private Set<ManufacturingPlant> manufacturingPlants;

	public Vendor() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Vendor(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<CarModel> getModels() {
		return models;
	}

	public void setModels(Set<CarModel> models) {
		this.models = models;
	}

	public Set<ManufacturingPlant> getManufacturingPlants() {
		return manufacturingPlants;
	}

	public void setManufacturingPlants(Set<ManufacturingPlant> manufacturingPlants) {
		this.manufacturingPlants = manufacturingPlants;
	}

	@Override
	public String toString() {
		return "Vendor{id=" + getId() + ", name=" + name + '}';
	}

	@Override
	public Integer getVersion() {
		return version;
	}
}
