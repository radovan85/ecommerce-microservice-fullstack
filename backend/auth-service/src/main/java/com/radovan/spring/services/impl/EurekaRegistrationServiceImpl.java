package com.radovan.spring.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radovan.spring.services.EurekaRegistrationService;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Service
public class EurekaRegistrationServiceImpl implements EurekaRegistrationService {

	private static final String EUREKA_SERVER_URL = "http://eureka-server:8761/eureka/apps";

	@Autowired
	private RestTemplate restTemplate;

	@Override
	@Scheduled(fixedRate = 30000L)
	public void registerService() {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			// Dinamičko dohvaćanje IP adrese i hostname-a
			String hostname = InetAddress.getLocalHost().getHostName();
			String ipAddr = InetAddress.getLocalHost().getHostAddress();

			String appName = "auth-service";
			String instanceId = appName + "-01";
			int port = 8080;

			// Kreiranje podataka za registraciju
			Map<String, Object> instanceData = new HashMap<>();
			instanceData.put("instanceId", instanceId);
			instanceData.put("app", appName);
			instanceData.put("hostName", hostname); // Koristi hostname
			instanceData.put("ipAddr", ipAddr); // Dinamički dohvaćena IP adresa
			instanceData.put("statusPageUrl", "http://" + ipAddr + ":" + port + "/info");
			instanceData.put("healthCheckUrl", "http://" + ipAddr + ":" + port + "/api/health");
			instanceData.put("homePageUrl", "http://" + ipAddr + ":" + port + "/");
			instanceData.put("vipAddress", appName);
			instanceData.put("secureVipAddress", appName);
			instanceData.put("leaseRenewalIntervalInSeconds", 30);
			instanceData.put("leaseExpirationDurationInSeconds", 90);

			// Dodavanje porta
			Map<String, Object> portMap = new HashMap<>();
			portMap.put("$", port);
			instanceData.put("port", portMap);

			Map<String, Object> securePortMap = new HashMap<>();
			securePortMap.put("$", 0);
			instanceData.put("securePort", securePortMap);

			// Dodavanje metadata
			Map<String, Object> metadata = new HashMap<>();
			metadata.put("management.port", port);
			instanceData.put("metadata", metadata);

			// Dodavanje dataCenterInfo
			Map<String, Object> dataCenterInfo = new HashMap<>();
			dataCenterInfo.put("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo");
			dataCenterInfo.put("name", "MyOwn");
			instanceData.put("dataCenterInfo", dataCenterInfo);

			// Glavni JSON objekat sa "instance" ključem
			Map<String, Object> registrationData = new HashMap<>();
			registrationData.put("instance", instanceData);

			// Konverzija u JSONNode
			JsonNode jsonPayload = objectMapper.valueToTree(registrationData);

			// Postavljanje Content-Type zaglavlja
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Kreiranje HttpEntity sa JSON telom i zaglavljem
			HttpEntity<JsonNode> requestEntity = new HttpEntity<>(jsonPayload, headers);

			// Slanje POST zahteva
			String registrationUrl = EUREKA_SERVER_URL + "/" + appName;
			restTemplate.postForEntity(registrationUrl, requestEntity, JsonNode.class);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to register service with Eureka", e);
		}
	}
}