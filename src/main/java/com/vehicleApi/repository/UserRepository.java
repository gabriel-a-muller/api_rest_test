package com.vehicleApi.repository;

import java.util.List;

import com.vehicleApi.model.User;

public interface UserRepository {

	User getUserById(int id);
	
	User getUserByName(String name);
	
	User getUserByEmail(String email);
	
	User getUserByCpf(String cpf);
	
	public List<User> getAll();
	
	User saveUser(User user);
	
	User updateUser(User user);
	
	void deleteUser(User user);

}
