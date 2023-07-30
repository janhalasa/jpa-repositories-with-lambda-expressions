package com.halasa.criterialambda.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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
