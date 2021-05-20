package com.vehicleApi.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.vehicleApi.model.User;
import com.vehicleApi.model.Vehicle;

@Repository
public class VehicleRepositoryImpl implements VehicleRepository{

	private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("MyDatabase");
	private static EntityManager em = entityManagerFactory.createEntityManager();
	
	public VehicleRepositoryImpl() {
		
	}
	
	public VehicleRepositoryImpl(EntityManager em) {
		VehicleRepositoryImpl.em = em;
	}
	
	@Override
	public Vehicle getVehicleById(int id) { 
		return em.find(Vehicle.class, id);
	}
	
	@Override
	public List<Vehicle> getVehicleByUser(User user) {
		TypedQuery<Vehicle> query = em.createQuery("SELECT v FROM Vehicle v WHERE v.user = :user", Vehicle.class);
		query.setParameter("user", user);
		return query.getResultList();
	}
	
	@Override
	public Vehicle saveVehicle(Vehicle vehicle) {
		em.persist(vehicle);
		return vehicle;
	}
	
	@Override
	public Vehicle updateVehicle(Vehicle vehicle) {
		em.merge(vehicle);
		return vehicle;
	}
	
	@Override
	public void deleteVehicle(Vehicle vehicle) {
		if (em.contains(vehicle)) {
			em.remove(vehicle);
		} else {
			em.merge(vehicle);
		}
	}
	
	@Override
	public List<Vehicle> getAll(){
		TypedQuery<Vehicle> query = em.createQuery("SELECT v FROM Vehicle v", Vehicle.class);
		return query.getResultList();
	}
	
}
