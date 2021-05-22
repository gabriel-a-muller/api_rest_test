package com.vehicleApi.model;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "vehicle")
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@JsonBackReference
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	private String brand;
	
	private int year;
	
	private String model;
	
	private String price;
	
	private int day_rotation;
	
	private boolean rotation_active;
	
	Vehicle() {
		
	}
	
	public Vehicle(User user) {
		this.setUser(user);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}
	
	public int getDay_rotation() {
		return day_rotation;
	}

	public void setDay_rotation(int day_rotation) {
		this.day_rotation = day_rotation;
	}

	public boolean isRotation_active() {
		return rotation_active;
	}

	public void setRotation_active(boolean rotation_active) {
		this.rotation_active = rotation_active;
	}
	
	public String toString() {
		String s = "";
		StringBuilder strBuilder = new StringBuilder(s);
		strBuilder.append("Vehicle: ");
		strBuilder.append(this.brand + " ");
		strBuilder.append(this.model + " ");
		strBuilder.append(this.year + " ");
		strBuilder.append(this.user.getId());
		return strBuilder.toString();
	}
	
}
