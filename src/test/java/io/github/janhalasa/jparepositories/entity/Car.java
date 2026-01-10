package io.github.janhalasa.jparepositories.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author janhalasa
 */
@Entity
public class Car {

	@Id
	@GeneratedValue
	private Long id;

	private String color;

	@ManyToOne
	@JoinColumn(name="model_id", nullable=false)
	private CarModel model;

	public Car() {
	}

	public Car(String color, CarModel model) {
		this.color = color;
		this.model = model;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getColor() {
		return color;
	}

	public void setColor(String colour) {
		this.color = colour;
	}
	
	public CarModel getModel() {
		return model;
	}

	public void setModel(CarModel model) {
		this.model = model;
	}

	@Override
	public String toString() {
		return "Car{id=" + getId() + ", colour=" + color + ", model=" + model + '}';
	}

	

}
