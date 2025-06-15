package com.radovan.spring.converter;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radovan.spring.dto.CustomerDto;
import com.radovan.spring.dto.ShippingAddressDto;
import com.radovan.spring.entity.CustomerEntity;
import com.radovan.spring.entity.ShippingAddressEntity;
import com.radovan.spring.repositories.CustomerRepository;
import com.radovan.spring.repositories.ShippingAddressRepository;

@Component
public class TempConverter {

	private ModelMapper mapper;
	private ShippingAddressRepository addressRepository;
	private CustomerRepository customerRepository;

	@Autowired
	private void initialize(ModelMapper mapper, ShippingAddressRepository addressRepository,
			CustomerRepository customerRepository) {
		this.mapper = mapper;
		this.addressRepository = addressRepository;
		this.customerRepository = customerRepository;
	}

	public CustomerDto customerEntityToDto(CustomerEntity customer) {
		CustomerDto returnValue = mapper.map(customer, CustomerDto.class);
		Optional<ShippingAddressEntity> addressOptional = Optional.ofNullable(customer.getShippingAddress());
		if (addressOptional.isPresent()) {
			returnValue.setShippingAddressId(addressOptional.get().getShippingAddressId());
		}

		return returnValue;
	}

	public CustomerEntity customerDtoToEntity(CustomerDto customer) {
		CustomerEntity returnValue = mapper.map(customer, CustomerEntity.class);
		Optional<Integer> addressIdOptional = Optional.ofNullable(customer.getShippingAddressId());
		if (addressIdOptional.isPresent()) {
			Integer addressId = addressIdOptional.get();
			ShippingAddressEntity addressEntity = addressRepository.findById(addressId).orElse(null);
			if (addressEntity != null) {
				returnValue.setShippingAddress(addressEntity);
			}
		}

		return returnValue;
	}

	public ShippingAddressDto addressEntityToDto(ShippingAddressEntity address) {
		ShippingAddressDto returnValue = mapper.map(address, ShippingAddressDto.class);
		Optional<CustomerEntity> customerOptional = Optional.ofNullable(address.getCustomer());
		if (customerOptional.isPresent()) {
			returnValue.setCustomerId(customerOptional.get().getCustomerId());
		}

		return returnValue;
	}

	public ShippingAddressEntity addressDtoToEntity(ShippingAddressDto address) {
		ShippingAddressEntity returnValue = mapper.map(address, ShippingAddressEntity.class);
		Optional<Integer> customerIdOptional = Optional.ofNullable(address.getCustomerId());
		if (customerIdOptional.isPresent()) {
			Integer customerId = customerIdOptional.get();
			CustomerEntity customerEntity = customerRepository.findById(customerId).orElse(null);
			if (customerEntity != null) {
				returnValue.setCustomer(customerEntity);
			}
		}

		return returnValue;
	}
}
