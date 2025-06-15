package com.radovan.spring.services;

import java.util.List;

import com.radovan.spring.dto.ShippingAddressDto;

public interface ShippingAddressService {

	ShippingAddressDto getAddressById(Integer addressId);

	ShippingAddressDto updateAddress(ShippingAddressDto address, Integer addressId);

	List<ShippingAddressDto> listAll();
}
