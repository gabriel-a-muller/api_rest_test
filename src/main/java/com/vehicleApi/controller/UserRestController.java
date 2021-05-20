package com.vehicleApi.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vehicleApi.model.User;
import com.vehicleApi.model.Vehicle;
import com.vehicleApi.repository.UserRepositoryImpl;
import com.vehicleApi.repository.VehicleRepositoryImpl;

@RestController
@RequestMapping("/user")
public class UserRestController {

	@Autowired
	VehicleRepositoryImpl vehicleRepo;

	@Autowired
	UserRepositoryImpl userRepo;

	@RequestMapping(path="/vehicle/{id}")
	public List<Vehicle> listUserVehicles(@PathVariable int id) {
		User user = userRepo.getUserById(id);
		return vehicleRepo.getAll();
	}

	@GetMapping
	public List<User> listAllUsers() {
		return userRepo.getAll();
	}

	private String formatCpf(String cpf) {
		if (cpf.length() <= 14) {
			String new_cpf = "";
			new_cpf = cpf.replace(".", "");
			new_cpf = new_cpf.replace("-", "");
			return new_cpf;
		} else {
			return null;
		}
	}

	@PostMapping
	public ResponseEntity<?> add(@RequestBody User user, HttpServletResponse response) {
		//Format CPF string
		String new_cpf = formatCpf(user.getCpf());
		if (new_cpf != null) {
			user.setCpf(new_cpf);
		} else {
			user.setResponse_error("This CPF appears to be invalid!");
			return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
		}
		//CHECK unique CPF
		if (userRepo.getUserByCpf(user.getCpf()) != null){
			user.setResponse_error("This cpf is already registred!");
			return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
		//CHECK unique email
		} else if (userRepo.getUserByEmail(user.getEmail()) != null) {
			user.setResponse_error("This email is already registred!");
			return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
		} else {
			System.out.println("I am here at the Rest Controller");
			if (userRepo.saveUser(user) != null) {
				return new ResponseEntity<>(user, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(user, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

}
