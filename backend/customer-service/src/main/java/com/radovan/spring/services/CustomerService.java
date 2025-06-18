package com.radovan.spring.services;

import java.util.List;

import com.radovan.spring.dto.CustomerDto;
import com.radovan.spring.utils.RegistrationForm;

public interface CustomerService {

	CustomerDto addCustomer(RegistrationForm form) throws IllegalStateException, InterruptedException;

	CustomerDto getCustomerById(Integer customerId);

	CustomerDto getCustomerByUserId(Integer userId);

	List<CustomerDto> listAll();

	void removeCustomer(Integer customerId);

	CustomerDto updtadeCustomer(CustomerDto customer);

	CustomerDto getCurrentCustomer();

	void suspendCustomer(Integer customerId);

	void reactivateCustomer(Integer customerId);
}
