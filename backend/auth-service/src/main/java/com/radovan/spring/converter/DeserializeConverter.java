package com.radovan.spring.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radovan.spring.dto.UserDto;

@Component
public class DeserializeConverter {

	private ObjectMapper objectMapper;

	@Autowired
	private void initialize(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public UserDto payloadToUserDto(String payloadData) {
		try {
			return objectMapper.readValue(payloadData, UserDto.class); // ✅ Konverzija JSON -> UserDto
		} catch (Exception e) {
			e.printStackTrace();
			return null; // Možeš dodati custom error handling
		}
	}

	public String deserializeUser(UserDto userDto) {
		try {
			return objectMapper.writeValueAsString(userDto);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
