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
	
	private int price;
	
	Vehicle() {
		
	}
	
	public Vehicle(User user) {
		this.setUser(user);
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

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	
}
