package com.radovan.play.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radovan.play.services.EurekaServiceDiscovery;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

@Singleton
public class EurekaServiceDiscoveryImpl implements EurekaServiceDiscovery {

    private static final String EUREKA_API_SERVICES_URL = "http://localhost:8761/eureka/apps";
    private static final String K8S_API_BASE = "https://kubernetes.default.svc/api/v1/namespaces/default/services/";

    private final WSClient wsClient;
    private final ObjectMapper objectMapper;

    @Inject
    public EurekaServiceDiscoveryImpl(WSClient wsClient) {
        this.wsClient = wsClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getServiceUrl(String serviceName) {
        boolean runningInK8s = System.getenv("KUBERNETES_SERVICE_HOST") != null;

        try {
            if (runningInK8s) {
                int port = fetchK8sServicePort(serviceName);
                return "http://" + serviceName + ":" + port;
            } else {
                return fetchEurekaServiceUrl(serviceName);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to resolve service URL for '" + serviceName + "': " + e.getMessage());
            throw new RuntimeException("Service resolution failed for '" + serviceName + "'", e);
        }
    }

    // 🔧 Eureka logic for local environment
    private String fetchEurekaServiceUrl(String serviceName) throws IOException {
        String serviceUrl = EUREKA_API_SERVICES_URL + "/" + serviceName;
        WSResponse rawResponse = wsClient.url(serviceUrl)
                .setRequestTimeout(5000)
                .addHeader("Accept", "application/json")
                .get()
                .toCompletableFuture()
                .join();

        if (rawResponse.getStatus() != 200) {
            throw new RuntimeException("Eureka registry returned status: " + rawResponse.getStatus());
        }

        JsonNode responseJson = objectMapper.readTree(rawResponse.getBody());
        JsonNode application = responseJson.path("application");
        if (application.isMissingNode()) {
            throw new RuntimeException("Service '" + serviceName + "' not found in Eureka registry.");
        }

        Iterator<JsonNode> instances = application.path("instance").elements();
        if (!instances.hasNext()) {
            throw new RuntimeException("No instances registered for service '" + serviceName + "'.");
        }

        JsonNode instance = instances.next();
        String address = instance.path("hostName").asText();
        JsonNode portNode = instance.path("port").path("$");

        if (address == null || portNode.isMissingNode() || !portNode.canConvertToInt()) {
            throw new RuntimeException("Invalid Eureka instance data for service: " + serviceName);
        }

        int port = portNode.asInt();
        return "http://" + address + ":" + port;
    }

    // 🔧 K8s logic for cluster environment
    private int fetchK8sServicePort(String serviceName) throws IOException {
        String tokenPath = "/var/run/secrets/kubernetes.io/serviceaccount/token";
        if (!Files.exists(Paths.get(tokenPath))) {
            throw new RuntimeException("K8s token not found — not running in cluster or token not mounted.");
        }

        String token = Files.readString(Paths.get(tokenPath));
        String url = K8S_API_BASE + serviceName;

        WSResponse response = wsClient.url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .toCompletableFuture()
                .join();

        JsonNode portNode = objectMapper.readTree(response.getBody())
                .path("spec").path("ports").get(0).path("port");

        if (portNode.isMissingNode() || !portNode.canConvertToInt()) {
            throw new RuntimeException("Port missing or invalid in K8s service spec for '" + serviceName + "'.");
        }

        return portNode.asInt();
    }
}
