package com.radovan.spring.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.radovan.spring.converter.TempConverter;
import com.radovan.spring.dto.ShippingAddressDto;
import com.radovan.spring.entity.ShippingAddressEntity;
import com.radovan.spring.exceptions.InstanceUndefinedException;
import com.radovan.spring.repositories.ShippingAddressRepository;
import com.radovan.spring.services.ShippingAddressService;

@Service
public class ShippingAddressServiceImpl implements ShippingAddressService {

	private ShippingAddressRepository addressRepository;
	private TempConverter tempConverter;

	@Autowired
	private void initialize(ShippingAddressRepository addressRepository, TempConverter tempConverter) {
		this.addressRepository = addressRepository;
		this.tempConverter = tempConverter;
	}

	@Override
	public ShippingAddressDto getAddressById(Integer addressId) {
		ShippingAddressEntity addressEntity = addressRepository.findById(addressId)
				.orElseThrow(() -> new InstanceUndefinedException(new Error("The address has not been found!")));
		return tempConverter.addressEntityToDto(addressEntity);
	}

	@Override
	public ShippingAddressDto updateAddress(ShippingAddressDto address, Integer addressId) {
		ShippingAddressDto currentAddress = getAddressById(addressId);
		address.setShippingAddressId(currentAddress.getShippingAddressId());
		address.setCustomerId(currentAddress.getCustomerId());
		ShippingAddressEntity updatedAddress = addressRepository
				.save(tempConverter.addressDtoToEntity(address));
		return tempConverter.addressEntityToDto(updatedAddress);
	}

	@Override
	public List<ShippingAddressDto> listAll() {
		List<ShippingAddressEntity> allAddresses = addressRepository.findAll();
		return allAddresses.stream().map(tempConverter::addressEntityToDto).collect(Collectors.toList());
	}

}