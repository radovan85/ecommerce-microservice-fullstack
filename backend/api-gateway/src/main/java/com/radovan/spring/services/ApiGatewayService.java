package com.radovan.spring.services;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import jakarta.servlet.http.HttpServletRequest;

public interface ApiGatewayService {

	ResponseEntity<String> forwardRequest(String serviceName, HttpServletRequest request) throws JsonMappingException, JsonProcessingException;

}
