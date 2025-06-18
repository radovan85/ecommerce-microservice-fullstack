package com.radovan.spring.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.radovan.spring.services.ApiGatewayService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/**") // Hvata sve rute pod /api/
@CrossOrigin(value = "*")
public class ApiGatewayController {

	@Autowired
	private ApiGatewayService apiGatewayService;
	// @Autowired
	// private ObjectMapper objectMapper;

	@RequestMapping
	public ResponseEntity<String> proxyRequest(HttpServletRequest request)
			throws JsonMappingException, JsonProcessingException {
		String requestUri = request.getRequestURI(); // /api/products ili /api/categories
		String[] pathSegments = requestUri.split("/");

		if (pathSegments.length < 3) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid API request format");
		}

		String firstSegment = pathSegments[2]; // products ili categories

		// Mapiranje segmenta na servis
		String serviceName = mapSegmentToService(firstSegment);
		if (serviceName == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unknown service for segment: " + firstSegment);
		}

		return apiGatewayService.forwardRequest(serviceName, request);

	}

	private String mapSegmentToService(String segment) {
		Map<String, String> serviceMappings = Map.of("products", "product-service", "categories", "product-service",
				"auth", "auth-service", "cart", "cart-service", "order", "order-service", "customers",
				"customer-service", "addresses", "customer-service");

		return serviceMappings.get(segment);
	}
}
