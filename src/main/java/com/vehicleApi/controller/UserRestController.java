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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vehicleApi.model.ServerResponse;
import com.vehicleApi.model.User;
import com.vehicleApi.repository.UserRepositoryImpl;
import com.vehicleApi.repository.VehicleRepositoryImpl;

@RestController
public class UserRestController {

	@Autowired
	VehicleRepositoryImpl vehicleRepo;

	@Autowired
	UserRepositoryImpl userRepo;

	@GetMapping
	public List<User> listAllUsers() {
		return userRepo.getAll();
	}
	
	@RequestMapping(path="/user/{id}", method = RequestMethod.GET)
	public User getUser(@PathVariable int id, HttpServletResponse response) {
		return userRepo.getUserById(id);
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
			ServerResponse serverResponse = new ServerResponse(user, "This CPF appears to be invalid!");
			return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);
		}

		//CHECK unique CPF
		if (userRepo.getUserByCpf(user.getCpf()) != null){
			ServerResponse serverResponse = new ServerResponse(user, "This cpf is already registred!");
			return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);

		//CHECK unique email
		} else if (userRepo.getUserByEmail(user.getEmail()) != null) {
			ServerResponse serverResponse = new ServerResponse(user, "This email is already registred!");
			return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);
		} else {
			if (userRepo.saveUser(user) != null) {
				ServerResponse serverResponse = new ServerResponse(user, "User created successfuly");
				return new ResponseEntity<>(serverResponse, HttpStatus.OK);
			} else {
				ServerResponse serverResponse = new ServerResponse(user, "Server Internal Error!");
				return new ResponseEntity<>(serverResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

}
