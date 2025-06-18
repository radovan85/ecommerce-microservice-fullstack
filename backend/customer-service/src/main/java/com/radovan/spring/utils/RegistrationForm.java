package com.radovan.spring.utils;

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;
import com.radovan.spring.dto.CustomerDto;
import com.radovan.spring.dto.ShippingAddressDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class RegistrationForm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Valid
	@NotNull
	private JsonNode user;

	@Valid
	@NotNull
	private CustomerDto customer;

	@Valid
	@NotNull
	private ShippingAddressDto shippingAddress;

	public JsonNode getUser() {
		return user;
	}

	public void setUser(JsonNode user) {
		this.user = user;
	}

	public CustomerDto getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerDto customer) {
		this.customer = customer;
	}

	public ShippingAddressDto getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(ShippingAddressDto shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

}