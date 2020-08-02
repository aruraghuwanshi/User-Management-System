package com.appsdeveloperblog.app.ws.service;

import java.util.List;

import com.appsdeveloperblog.app.ws.shared.dto.AddressDto;


public interface AddressService {
	List<AddressDto> getAddresses(String userId);
	AddressDto getAddress(String addressId);
}
