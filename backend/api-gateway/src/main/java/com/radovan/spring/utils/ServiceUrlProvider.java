package com.radovan.spring.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radovan.spring.services.EurekaServiceDiscovery;

@Component
public class ServiceUrlProvider {

	private final Map<String, String> cachedServiceUrls = new ConcurrentHashMap<>();
	private EurekaServiceDiscovery eurekaServiceDiscovery;

	@Autowired
	private void initialize(EurekaServiceDiscovery eurekaServiceDiscovery) {
		this.eurekaServiceDiscovery = eurekaServiceDiscovery;
	}

	public String getServiceUrl(String serviceName) {
		return cachedServiceUrls.computeIfAbsent(serviceName, key -> {
			try {
				String serviceUrl = eurekaServiceDiscovery.getServiceUrl(serviceName);
				validateUrl(serviceUrl, serviceName);
				return serviceUrl;
			} catch (RuntimeException e) {
				System.err.println("Failed to retrieve service URL for: " + serviceName + " - " + e.getMessage());
				throw e;
			}
		});
	}

	public String getAuthServiceUrl() {
		return getServiceUrl("auth-service");
	}

	public String getCustomerServiceUrl() {
		return getServiceUrl("customer-service");
	}

	public String getOrderServiceUrl() {
		return getServiceUrl("order-service");
	}

	public String getCartServiceUrl() {
		return getServiceUrl("cart-service");
	}

	public String getProductServiceUrl() {
		return getServiceUrl("product-service");
	}

	public String getGatewayServiceUrl() {
		return getServiceUrl("api-gateway");
	}

	private void validateUrl(String url, String serviceName) {
		if (url == null || !url.startsWith("http")) {
			throw new IllegalArgumentException("Invalid URL for " + serviceName + ": " + url);
		}
	}

}
