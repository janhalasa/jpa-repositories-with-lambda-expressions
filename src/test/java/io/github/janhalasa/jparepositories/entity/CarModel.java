package io.github.janhalasa.jparepositories.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 *
 * @author janhalasa
 */
@Entity
public class CarModel {

	@Id
	@GeneratedValue
	private Long id;
	
	private String name;
	
	@ManyToOne
	private Vendor vendor;
	
	@OneToMany
	@JoinColumn(name="carmodel_id", nullable=true)
	private Set<Car> cars;

	public CarModel() {
	}

	public CarModel(String name, Vendor vendor) {
		this.name = name;
		this.vendor = vendor;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public Set<Car> getCars() {
		return cars;
	}

	public void setCars(Set<Car> cars) {
		this.cars = cars;
	}

	@Override
	public String toString() {
		return "CarModel{id=" + getId() + ", name=" + name + ", vendor=" + vendor + '}';
	}
	
}
