package com.vehicleApi.repository;

import com.vehicleApi.model.User;

public interface UserRepository {

	User getUserById(int id);
	
	User getUserByName(String name);
	
	User saveUser(User user);
	
	User updateUser(User user);
	
	void deleteUser(User user);

}
