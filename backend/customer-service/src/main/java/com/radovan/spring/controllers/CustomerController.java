package com.radovan.spring.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.radovan.spring.dto.CustomerDto;
import com.radovan.spring.exceptions.DataNotValidatedException;
import com.radovan.spring.services.CustomerService;
import com.radovan.spring.utils.RegistrationForm;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/customers")
public class CustomerController {

	@Autowired
	private CustomerService customerService;

	@PreAuthorize(value = "hasAuthority('ROLE_USER')")
	@GetMapping(value = "/getCurrentCustomer")
	public ResponseEntity<CustomerDto> getCurrentCustomer() {
		CustomerDto customer = customerService.getCurrentCustomer();
		return new ResponseEntity<>(customer, HttpStatus.OK);
	}

	@PreAuthorize(value = "hasAuthority('ROLE_USER')")
	@PutMapping
	public ResponseEntity<String> updateCustomer(@Validated @RequestBody CustomerDto customer,
			HttpServletRequest request, Errors errors) {
		if (errors.hasErrors()) {
			throw new DataNotValidatedException(new Error("The customer has not been validated!"));
		}

		customerService.updtadeCustomer(customer);
		return new ResponseEntity<>("The customer has been updated!", HttpStatus.OK);
	}

	@PostMapping(value = "/register")
	public ResponseEntity<String> createCustomer(@RequestBody @Validated RegistrationForm form, Errors errors)
			throws IllegalStateException, InterruptedException {
		if (errors.hasErrors()) {
			throw new DataNotValidatedException(new Error("The customer has not been validated"));
		}

		customerService.addCustomer(form);

		return new ResponseEntity<>("You have been registered successfully!", HttpStatus.OK);
	}

	@PreAuthorize(value = "hasAuthority('ROLE_ADMIN')")
	@GetMapping
	public ResponseEntity<List<CustomerDto>> getAllCustomers() {
		List<CustomerDto> allCustomers = customerService.listAll();
		return new ResponseEntity<>(allCustomers, HttpStatus.OK);
	}

	@PreAuthorize(value = "hasAuthority('ROLE_ADMIN')")
	@GetMapping(value = "/{customerId}")
	public ResponseEntity<CustomerDto> getCustomerDetails(@PathVariable("customerId") Integer customerId) {
		CustomerDto customer = customerService.getCustomerById(customerId);
		return new ResponseEntity<>(customer, HttpStatus.OK);
	}

	@PreAuthorize(value = "hasAuthority('ROLE_ADMIN')")
	@DeleteMapping(value = "/{customerId}")
	public ResponseEntity<String> deleteCustomer(@PathVariable("customerId") Integer customerId) {
		customerService.removeCustomer(customerId);
		return new ResponseEntity<>("The customer with id " + customerId + " has been permanently deleted!",
				HttpStatus.OK);
	}

	@PreAuthorize(value = "hasAuthority('ROLE_ADMIN')")
	@PutMapping(value = "/suspend/{customerId}")
	public ResponseEntity<String> suspendCustomer(@PathVariable("customerId") Integer customerId) {
		customerService.suspendCustomer(customerId);
		return new ResponseEntity<>("The customer with id " + customerId + " has been suspended!", HttpStatus.OK);
	}

	@PreAuthorize(value = "hasAuthority('ROLE_ADMIN')")
	@PutMapping(value = "/reactivate/{customerId}")
	public ResponseEntity<String> reactivateCustomer(@PathVariable("customerId") Integer customerId) {
		customerService.reactivateCustomer(customerId);
		return new ResponseEntity<>("The customer with id " + customerId + " has been reactivated!", HttpStatus.OK);
	}
}
