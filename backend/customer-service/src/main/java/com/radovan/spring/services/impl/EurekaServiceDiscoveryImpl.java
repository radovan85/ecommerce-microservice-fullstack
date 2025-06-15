package com.radovan.spring.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radovan.spring.services.EurekaServiceDiscovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;

@Service
public class EurekaServiceDiscoveryImpl implements EurekaServiceDiscovery {

	private static final String EUREKA_API_SERVICES_URL = "http://localhost:8761/eureka/apps";
	private RestTemplate restTemplate;
	private ObjectMapper objectMapper;

	@Autowired
	private void initialize(RestTemplate restTemplate, ObjectMapper objectMapper) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public String getServiceUrl(String serviceName) {
		try {
			String url = EUREKA_API_SERVICES_URL + "/" + serviceName;
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
			String rawBody = response.getBody();

			if (rawBody == null || rawBody.trim().isEmpty()) {
				throw new RuntimeException("Eureka registry ne odgovara ispravno!");
			}

			// ✅ Parsiranje JSON odgovora
			JsonNode responseJson = objectMapper.readTree(rawBody);

			JsonNode application = responseJson.get("application");
			if (application == null) {
				throw new RuntimeException("Servis " + serviceName + " nije pronađen u Eureka registry!");
			}

			Iterator<JsonNode> instances = application.get("instance").elements();
			while (instances.hasNext()) {
				JsonNode instance = instances.next();
				String address = instance.get("hostName").asText();
				JsonNode portNode = instance.get("port");

				// ✅ Ispravno dohvatamo port iz JSON strukture
				int port = portNode.get("$").asInt();

				if (address == null || port == 0) {
					throw new RuntimeException("Invalid service data: " + serviceName);
				}

				return "http://" + address + ":" + port;
			}

			throw new RuntimeException("Service not found: " + serviceName);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to fetch service URL from Eureka registry", e);
		}
	}
}
