package com.radovan.spring.services.impl;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.radovan.spring.broker.CustomerNatsSender;
import com.radovan.spring.converter.DeserializeConverter;
import com.radovan.spring.converter.TempConverter;
import com.radovan.spring.dto.CustomerDto;
import com.radovan.spring.entity.CustomerEntity;
import com.radovan.spring.entity.ShippingAddressEntity;
import com.radovan.spring.exceptions.ExistingInstanceException;
import com.radovan.spring.exceptions.InstanceUndefinedException;
import com.radovan.spring.repositories.CustomerRepository;
import com.radovan.spring.repositories.ShippingAddressRepository;
import com.radovan.spring.services.CustomerService;
import com.radovan.spring.utils.NatsUtils;
import com.radovan.spring.utils.RegistrationForm;

import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Subscription;

@Service
public class CustomerServiceImpl implements CustomerService {

	private CustomerRepository customerRepository;
	private TempConverter tempConverter;
	private NatsUtils natsUtils;
	private DeserializeConverter deserializeConverter;
	private ShippingAddressRepository addressRepository;
	private CustomerNatsSender customerNatsSender;

	@Autowired
	private void initialize(CustomerRepository customerRepository, TempConverter tempConverter, NatsUtils natsUtils,
			DeserializeConverter deserializeConverter, ShippingAddressRepository addressRepository,
			CustomerNatsSender customerNatsSender) {
		this.customerRepository = customerRepository;
		this.tempConverter = tempConverter;
		this.natsUtils = natsUtils;
		this.deserializeConverter = deserializeConverter;
		this.addressRepository = addressRepository;
		this.customerNatsSender = customerNatsSender;
	}

	@Override
	@Transactional
	public CustomerDto addCustomer(RegistrationForm form) throws IllegalStateException, InterruptedException {

		Connection natsConnection = natsUtils.getConnection();
		JsonNode user = form.getUser();
		String jsonPayload = user.toString();
		natsConnection.publish("user.create", jsonPayload.getBytes(StandardCharsets.UTF_8));

		Subscription subscription = natsConnection.subscribe("user.response");
		Message responseMsg = subscription.nextMessage(Duration.ofSeconds(5));

		if (responseMsg == null) {
			throw new IllegalStateException("Timeout pri kreiranju usera");
		}

		String responseJson = new String(responseMsg.getData(), StandardCharsets.UTF_8);

		JsonNode responseNode = deserializeConverter.StringToJsonNode(responseJson);
		Integer statusCode = responseNode.get("status").asInt();

		if (statusCode == 409) {
			throw new ExistingInstanceException(new Error("Email exists already!"));
		}

		Integer userId = responseNode.get("id").asInt();
		natsConnection.publish("cart.create", userId.toString().getBytes(StandardCharsets.UTF_8));

		Subscription cartSubscription = natsConnection.subscribe("cart.response");
		Message cartMsg = cartSubscription.nextMessage(Duration.ofSeconds(5));

		if (cartMsg == null) {
			throw new IllegalStateException("Timeout pri kreiranju korpe");
		}

		String cartJson = new String(cartMsg.getData(), StandardCharsets.UTF_8);

		JsonNode cartResponseNode = deserializeConverter.StringToJsonNode(cartJson);

		if (!cartResponseNode.has("cartId")) { // ✅ Provera da `cartId` postoji, sprečava NullPointerException
			throw new IllegalStateException("Cart response does not contain `cartId`!");
		}

		Integer cartId = cartResponseNode.get("cartId").asInt();

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
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerDto getCustomerById(Integer customerId) {
		return customerRepository.findById(customerId).map(tempConverter::customerEntityToDto)
				.orElseThrow(() -> new InstanceUndefinedException(new Error("The customer has not been found!")));
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerDto getCustomerByUserId(Integer userId) {
		return customerRepository.findByUserId(userId).map(tempConverter::customerEntityToDto)
				.orElseThrow(() -> new InstanceUndefinedException(new Error("The customer has not been found!")));
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomerDto> listAll() {
		return customerRepository.findAll().stream().map(tempConverter::customerEntityToDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public CustomerDto updtadeCustomer(CustomerDto customer) {
		// TODO Auto-generated method stub
		CustomerDto currentCustomer = getCurrentCustomer();
		customer.setCartId(currentCustomer.getCartId());
		customer.setCustomerId(currentCustomer.getCustomerId());
		customer.setShippingAddressId(currentCustomer.getShippingAddressId());
		customer.setUserId(currentCustomer.getUserId());
		CustomerEntity updatedCustomer = customerRepository.saveAndFlush(tempConverter.customerDtoToEntity(customer));
		return tempConverter.customerEntityToDto(updatedCustomer);
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerDto getCurrentCustomer() {
		// ✅ Dobavljamo podatke o korisniku preko NATS-a
		JsonNode currentUserNode = customerNatsSender.retrieveCurrentUser();

		if (currentUserNode == null || !currentUserNode.has("id")) {
			throw new InstanceUndefinedException(new Error("No user ID found in response from NATS!"));
		}

		Integer userId = currentUserNode.get("id").asInt();
		return getCustomerByUserId(userId);
	}

	@Override
	@Transactional
	public void removeCustomer(Integer customerId) {
		CustomerDto customer = getCustomerById(customerId);
		customerRepository.deleteById(customerId);
		customerRepository.flush();
		customerNatsSender.sendDeleteOrdersByCartId(customer.getCartId());
		customerNatsSender.sendDeleteCartEvent(customer.getCartId());
		customerNatsSender.sendDeleteUserEvent(customer.getUserId());
	}

	@Override
	@Transactional
	public void suspendCustomer(Integer customerId) {
		CustomerDto customer = getCustomerById(customerId);
		customerNatsSender.sendSuspendUserEvent(customer.getUserId());
	}

	@Override
	@Transactional
	public void reactivateCustomer(Integer customerId) {
		CustomerDto customer = getCustomerById(customerId);
		customerNatsSender.sendReactivateUserEvent(customer.getUserId());
	}

}
