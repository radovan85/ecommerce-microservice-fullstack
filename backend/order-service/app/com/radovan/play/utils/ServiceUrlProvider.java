package com.radovan.play.utils;

import com.radovan.play.services.EurekaServiceDiscovery;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ServiceUrlProvider {

    private final Map<String, String> cachedServiceUrls = new ConcurrentHashMap<>();
    private  EurekaServiceDiscovery eurekaServiceDiscovery;

    @Inject
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

    private void validateUrl(String url, String serviceName) {
        if (url == null || !url.startsWith("http")) {
            throw new IllegalArgumentException("Invalid URL for " + serviceName + ": " + url);
        }
    }


}
