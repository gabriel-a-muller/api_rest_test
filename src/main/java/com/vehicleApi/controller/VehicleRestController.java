package com.vehicleApi.controller;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vehicleApi.model.ServerResponse;
import com.vehicleApi.model.User;
import com.vehicleApi.model.Vehicle;
import com.vehicleApi.repository.UserRepositoryImpl;
import com.vehicleApi.repository.VehicleRepositoryImpl;

@RestController
public class VehicleRestController {

	@Autowired
	VehicleRepositoryImpl vehicleRepo;

	@Autowired
	UserRepositoryImpl userRepo;

	private String apiUrl = "https://parallelum.com.br/fipe/api/v1/carros/marcas";
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public String getApiUrl() {
		return apiUrl;
	}

	private String makeApiRequest(String strRequest) {
		OkHttpClient client = new OkHttpClient();
		
		Request request = new Request.Builder()
			.url(strRequest)
			.build();
		Response response = null;

		try {
			response = client.newCall(request).execute();
			String myResult = response.body().string();
			response.body().close();
			return myResult;
		} catch (IOException e) {
		    return "Error!";
		  }
	}
	
	private int getBrandCode(String brand) {
		String apiResult = makeApiRequest(getApiUrl());
		int code = -1;
		if (apiResult != "Error!") {
			code = 0;
			if (apiResult.isEmpty()) {
				return code = -1;
			} else if (apiResult.charAt(0) == '[') {
				JSONArray apiArray = new JSONArray(apiResult);
				for(int i=0; i < apiArray.length(); i++)   
				{
					JSONObject object = apiArray.getJSONObject(i);
					if (object.getString("nome").equals(brand)) {
						code = Integer.parseInt(object.getString("codigo"));
						System.out.println(code);
					}
				}
			}
		}
		return code;
	}
	
	private int getModelCode(String model, int code) {
		setApiUrl(getApiUrl() + "/" + code + "/modelos");
		String modelResult = makeApiRequest(getApiUrl());
		int modelCode = -1;
		if (modelResult != "Error!") {
			JSONObject jsonResult = new JSONObject(modelResult);
			JSONArray Jarray = jsonResult.getJSONArray("modelos");
			for(int i=0; i < Jarray.length(); i++)   
			{
				JSONObject object = Jarray.getJSONObject(i);
				if (object.getString("nome").equals(model)) {
					modelCode = object.getInt("codigo");
					System.out.println(modelCode);
				}
			}
		}
		return modelCode;
	}
	
	private String getVehiclePrice(int modelCode, int year) {
		setApiUrl(getApiUrl() + "/" + modelCode + "/anos/" + year + "-3");
		String priceResult = makeApiRequest(getApiUrl());
		if (priceResult.isEmpty()) {
			return "Erro!";
		} else if (priceResult.charAt(0) == '{') {
			JSONObject jsonResult = new JSONObject(priceResult);
			return jsonResult.getString("Valor");
		} else {
			return "Erro!";
		}
	}
	
	private int getWeekDay(int year) {
		int lastDigit = year % 10;
		if (lastDigit == 0 || lastDigit == 1) {
			return Calendar.MONDAY;
		} else if (lastDigit == 2 || lastDigit == 3) {
			return Calendar.TUESDAY;
		} else if (lastDigit == 4 || lastDigit == 5) {
			return Calendar.WEDNESDAY;
		} else if (lastDigit == 6 || lastDigit == 7) {
			return Calendar.THURSDAY;
		} else if (lastDigit == 8 || lastDigit == 9) {
			return Calendar.FRIDAY;
		} else {
			return 0;
		}
	}
	
	private boolean getRotationActive(int dayRotation) {
		Date now = new Date();
		System.out.println(now);
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		if (c.get(Calendar.DAY_OF_WEEK) == dayRotation) {
			return true;
		} else {
			return false;
		}
	}
	
	@RequestMapping(path="/vehicle/{id}", method = RequestMethod.POST)
	public ResponseEntity<?> addVehicle(@RequestBody Vehicle vehicle, HttpServletResponse response, @PathVariable int id) {
		
		User user = userRepo.getUserById(id);
		if (user == null) {
			ServerResponse serverResponse = new ServerResponse(vehicle, "User not found!");
			return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);
		}
		
		//Logic to get the price of the vehicle
		int code = getBrandCode(vehicle.getBrand());
		if (code == 0) {
			ServerResponse serverResponse = new ServerResponse(vehicle, "Car brand not found!");
			return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);
		} else if (code == -1) {
			ServerResponse serverResponse = new ServerResponse(vehicle, "API not found");
			return new ResponseEntity<>(serverResponse, HttpStatus.NOT_FOUND);
		} else {
			int modelCode = getModelCode(vehicle.getModel(), code);
			if (modelCode == -1) {
				ServerResponse serverResponse = new ServerResponse(vehicle, "Car model not found!");
				return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);
			} else {
				String price = getVehiclePrice(modelCode, vehicle.getYear());
				vehicle.setPrice(price);
			}
		}
		//Set Day Rotation
		vehicle.setDay_rotation(getWeekDay(vehicle.getYear()));
		
		//Set Active Rotation
		vehicle.setRotation_active(getRotationActive(vehicle.getDay_rotation()));
		
		vehicle.setUser(user);
		
		if (vehicleRepo.saveVehicle(vehicle) != null) {
			ServerResponse serverResponse = new ServerResponse(vehicle, "Vehicle created successfuly!");
			return new ResponseEntity<>(serverResponse, HttpStatus.OK);
		} else {
			ServerResponse serverResponse = new ServerResponse(vehicle, "Server Internal Error!");
			return new ResponseEntity<>(serverResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
