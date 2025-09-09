package com.radovan.spring.services;

import java.util.List;

import com.radovan.spring.dto.CustomerDto;
import com.radovan.spring.utils.RegistrationForm;

public interface CustomerService {

	CustomerDto addCustomer(RegistrationForm form) throws IllegalStateException, InterruptedException;

	CustomerDto getCustomerById(Integer customerId);

	CustomerDto getCustomerByUserId(Integer userId);

	List<CustomerDto> listAll();

	CustomerDto updtadeCustomer(CustomerDto customer);

	CustomerDto getCurrentCustomer(String jwtToken);

	void removeCustomer(Integer customerId, String jwtToken);

	void suspendCustomer(Integer customerId, String jwtToken);

	void reactivateCustomer(Integer customerId, String jwtToken);
}
