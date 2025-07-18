package com.radovan.spring.services.impl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.radovan.spring.services.ApiGatewayService;
import com.radovan.spring.utils.ResponseHandler;
import com.radovan.spring.utils.ServiceUrlProvider;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ApiGatewayServiceImpl implements ApiGatewayService {

	private ServiceUrlProvider serviceUrlProvider;
	private RestTemplate restTemplate;
	private ResponseHandler responseHandler;

	// Cache za servis URL-ove (TTL se može dodati)
	private final Map<String, String> cachedServiceUrls = new ConcurrentHashMap<>();

	@Autowired
	private void initialize(ServiceUrlProvider serviceUrlProvider, RestTemplate restTemplate,
			ResponseHandler responseHandler) {
		this.serviceUrlProvider = serviceUrlProvider;
		this.restTemplate = restTemplate;
		this.responseHandler = responseHandler;

	}

	@Override
	public ResponseEntity<String> forwardRequest(String serviceName, HttpServletRequest request)
			throws JsonMappingException, JsonProcessingException {
		String serviceUrl = cachedServiceUrls.computeIfAbsent(serviceName,
				key -> serviceUrlProvider.getServiceUrl(serviceName));

		if (serviceUrl == null) {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Service " + serviceName + " not found");
		}

		HttpMethod method = HttpMethod.valueOf(request.getMethod());

		HttpHeaders headers = new HttpHeaders();
		// Kopiraj sve zaglavlja osim Content-Length (neka RestTemplate to sam obradi)
		Collections.list(request.getHeaderNames()).forEach(headerName -> {
			if (!"content-length".equalsIgnoreCase(headerName)) {
				headers.addAll(headerName, Collections.list(request.getHeaders(headerName)));
			}
		});

		// Obrada tela zahteva
		byte[] requestBody = null;
		if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
			try {
				requestBody = StreamUtils.copyToByteArray(request.getInputStream());
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Error reading request body: " + e.getMessage());
			}
		}

		HttpEntity<byte[]> entity = new HttpEntity<>(requestBody, headers);

		String fullUrl = serviceUrl + request.getRequestURI();

		// Konfiguriši RestTemplate da pravilno obrađuje chunked transfer encoding
		restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
		return responseHandler.fetchAndConvertToJson(fullUrl, method, entity);

	}
}
