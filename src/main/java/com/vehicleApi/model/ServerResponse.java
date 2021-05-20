package com.vehicleApi.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class ServerResponse {
	@JsonUnwrapped
	private final Object wrapped;
	private final String message;
	
	public ServerResponse(Object wrapped, String message) {
		this.wrapped = wrapped;
		this.message = message;
	}

	public Object getWrapped() {
		return wrapped;
	}

	public String getMessage() {
		return message;
	}
	
}
