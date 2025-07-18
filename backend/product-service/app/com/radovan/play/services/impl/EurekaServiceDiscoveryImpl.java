package com.radovan.play.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.radovan.play.services.EurekaServiceDiscovery;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import play.libs.ws.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

@Singleton
public class EurekaServiceDiscoveryImpl implements EurekaServiceDiscovery {

    private static final String EUREKA_API_SERVICES_URL = "http://eureka-server:8761/eureka/apps";
    private WSClient wsClient;

    @Inject
    private void initialize(WSClient wsClient) {
        this.wsClient = wsClient;
    }

    @Override
    public String getServiceUrl(String serviceName) {
        try {
            boolean runningInKubernetes = System.getenv("KUBERNETES_SERVICE_HOST") != null;
            String address;
            int port;

            if (runningInKubernetes) {
                address = serviceName; // DNS ime u K8s
                port = getK8sServicePort(serviceName);
            } else {
                JsonNode response = wsClient.url(EUREKA_API_SERVICES_URL + "/" + serviceName)
                        .get()
                        .toCompletableFuture()
                        .join()
                        .asJson();

                JsonNode application = response.get("application");
                if (application == null) {
                    throw new RuntimeException("Servis nije pronađen u Eureka registry: " + serviceName);
                }

                Iterator<JsonNode> instances = application.get("instance").elements();
                JsonNode instance = instances.next();
                address = instance.get("hostName").asText();
                port = instance.get("port").get("$").asInt();
            }

            return "http://" + address + ":" + port;

        } catch (Exception e) {
            System.err.println("❌ Greška u getServiceUrl za " + serviceName + ": " + e.getMessage());
            return "http://" + serviceName + ":8080"; // fallback URL
        }
    }

    private int getK8sServicePort(String serviceName) {
        try {
            String token = Files.readString(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token"));
            String url = "https://kubernetes.default.svc/api/v1/namespaces/default/services/" + serviceName;

            JsonNode response = wsClient.url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .get()
                    .toCompletableFuture()
                    .join()
                    .asJson();

            return response.path("spec").path("ports").get(0).path("port").asInt();

        } catch (IOException e) {
            System.err.println("❌ Token nije dostupan: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Neuspešan K8s port fetch za " + serviceName + ": " + e.getMessage());
        }

        return 8080; // fallback port
    }
}
