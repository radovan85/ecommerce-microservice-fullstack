package com.radovan.spring.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.radovan.spring.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.radovan.spring.broker.CustomerNatsSender;
import com.radovan.spring.converter.TempConverter;
import com.radovan.spring.dto.CustomerDto;
import com.radovan.spring.entity.CustomerEntity;
import com.radovan.spring.entity.ShippingAddressEntity;
import com.radovan.spring.exceptions.ExistingInstanceException;
import com.radovan.spring.exceptions.InstanceUndefinedException;
import com.radovan.spring.repositories.CustomerRepository;
import com.radovan.spring.repositories.ShippingAddressRepository;
import com.radovan.spring.services.CustomerService;
import com.radovan.spring.utils.RegistrationForm;

@Service
public class CustomerServiceImpl implements CustomerService {

	private CustomerRepository customerRepository;
	private TempConverter tempConverter;
	private ShippingAddressRepository addressRepository;
	private CustomerNatsSender customerNatsSender;

	@Autowired
	private void initialize(CustomerRepository customerRepository, TempConverter tempConverter,
			 ShippingAddressRepository addressRepository,
			CustomerNatsSender customerNatsSender) {
		this.customerRepository = customerRepository;
		this.tempConverter = tempConverter;
		this.addressRepository = addressRepository;
		this.customerNatsSender = customerNatsSender;
	}

	@Override
	public CustomerDto addCustomer(RegistrationForm form) throws IllegalStateException, InterruptedException {
		try {
			JsonNode user = form.getUser();
			int userId = customerNatsSender.sendUserCreate(user);
			int cartId = customerNatsSender.sendCartCreate();

			ShippingAddressEntity storedAddress = addressRepository
					.save(tempConverter.addressDtoToEntity(form.getShippingAddress()));

			CustomerDto customerDto = form.getCustomer();
			customerDto.setUserId(userId);
			customerDto.setCartId(cartId);
			customerDto.setShippingAddressId(storedAddress.getShippingAddressId());

			CustomerEntity storedCustomer = customerRepository.save(tempConverter.customerDtoToEntity(customerDto));

			storedAddress.setCustomer(storedCustomer);
			addressRepository.save(storedAddress);

			return tempConverter.customerEntityToDto(storedCustomer);

		} catch (ExistingInstanceException e) {
			throw new IllegalStateException("User already exists: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create customer: " + e.getMessage(), e);
		}
	}


	@Override
	public CustomerDto getCustomerById(Integer customerId) {
		return customerRepository.findById(customerId).map(tempConverter::customerEntityToDto)
				.orElseThrow(() -> new InstanceUndefinedException(new Error("The customer has not been found!")));
	}

	@Override
	public CustomerDto getCustomerByUserId(Integer userId) {
		return customerRepository.findByUserId(userId).map(tempConverter::customerEntityToDto)
				.orElseThrow(() -> new InstanceUndefinedException(new Error("The customer has not been found!")));
	}

	@Override
	public List<CustomerDto> listAll() {
		return customerRepository.findAll().stream().map(tempConverter::customerEntityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public CustomerDto updtadeCustomer(CustomerDto customer) {
		CustomerDto currentCustomer = getCurrentCustomer(TokenUtils.provideToken());
		customer.setCartId(currentCustomer.getCartId());
		customer.setCustomerId(currentCustomer.getCustomerId());
		customer.setShippingAddressId(currentCustomer.getShippingAddressId());
		customer.setUserId(currentCustomer.getUserId());
		CustomerEntity updatedCustomer = customerRepository.save(tempConverter.customerDtoToEntity(customer));
		return tempConverter.customerEntityToDto(updatedCustomer);
	}

	@Override
	public CustomerDto getCurrentCustomer(String jwtToken) {
		try {
			// ✅ Dobavljamo podatke o korisniku preko NATS-a
			JsonNode currentUserNode = customerNatsSender.retrieveCurrentUser(jwtToken);

			if (currentUserNode == null || !currentUserNode.has("id")) {
				throw new InstanceUndefinedException(new Error("No user ID found in response from NATS!"));
			}

			Integer userId = currentUserNode.get("id").asInt();
			return getCustomerByUserId(userId);

		} catch (Exception e) {
			throw new IllegalStateException("Failed to retrieve current user from NATS: " + e.getMessage(), e);
		}
	}


	@Override
	public void removeCustomer(Integer customerId, String jwtToken) {
		CustomerDto customer = getCustomerById(customerId);
		customerRepository.deleteById(customerId);

		try {
			customerNatsSender.sendDeleteAllOrders(customer.getCartId(), jwtToken);
			customerNatsSender.sendCartDelete(customer.getCartId(), jwtToken);
			customerNatsSender.sendDeleteUserEvent(customer.getUserId(), jwtToken);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to propagate customer deletion to NATS: " + e.getMessage(), e);
		}
	}


	@Override
	public void suspendCustomer(Integer customerId, String jwtToken) {
		CustomerDto customer = getCustomerById(customerId);
		customerNatsSender.sendSuspendUserEvent(customer.getUserId(),jwtToken);
	}

	@Override
	public void reactivateCustomer(Integer customerId, String jwtToken) {
		CustomerDto customer = getCustomerById(customerId);
		customerNatsSender.sendReactivateUserEvent(customer.getUserId(),jwtToken);
	}

}
