package com.radovan.spring.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radovan.spring.services.EurekaServiceDiscovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
				throw new RuntimeException("Eureka is not responding properly!");
			}

			// ✅ Parsiranje JSON odgovora
			JsonNode responseJson = objectMapper.readTree(rawBody);

			JsonNode application = responseJson.get("application");
			if (application == null) {
				throw new RuntimeException("Service " + serviceName + " has not been found in Eureka registry!");
			}

			Iterator<JsonNode> instances = application.get("instance").elements();
			while (instances.hasNext()) {
				JsonNode instance = instances.next();
				boolean runningInKubernetes = System.getenv("KUBERNETES_SERVICE_HOST") != null;
				String address = null;
				int port = 8080;
				if (runningInKubernetes) {
					address = serviceName;
					port = getK8sServicePort(address);
				} else {
					address = instance.get("hostName").asText();
					JsonNode portNode = instance.get("port");
					port = portNode.get("$").asInt();
				}

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

	private int getK8sServicePort(String serviceName) {
		try {
			// 🔒 Token iz ServiceAccount
			String token = Files.readString(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token"));

			// 🌐 K8s API URL
			String url = "https://kubernetes.default.svc/api/v1/namespaces/default/services/" + serviceName;

			// 📡 Header sa tokenom
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(token);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			// 🛠️ Bypass SSL verifikacije
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} };

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

			// 🚀 Gađanje API-ja
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<Void> entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

			JsonNode json = objectMapper.readTree(response.getBody());
			return json.path("spec").path("ports").get(0).path("port").asInt(); // npr. 8081

		} catch (Exception e) {
			System.err.println("❌ Failed to get K8s port for " + serviceName + ": " + e.getMessage());
			return 8080; // 🎯 Fallback port ako padne upit
		}
	}

}
