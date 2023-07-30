package com.halasa.criterialambda.entity;

import com.halasa.criterialambda.model.VersionAware;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.List;

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
	private List<CarModel> models;

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

	public List<CarModel> getModels() {
		return models;
	}

	public void setModels(List<CarModel> models) {
		this.models = models;
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
