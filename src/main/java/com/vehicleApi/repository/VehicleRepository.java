package com.vehicleApi.repository;

import java.util.List;

import com.vehicleApi.model.User;
import com.vehicleApi.model.Vehicle;

public interface VehicleRepository {

	Vehicle getVehicleById(int id);
	
	List<Vehicle> getVehicleByUser(User user);

	Vehicle saveVehicle(Vehicle vehicle);

	Vehicle updateVehicle(Vehicle vehicle);

	void deleteVehicle(Vehicle vehicle);
	
	List<Vehicle> getAll();

}
