package com.radovan.play.services.impl;

import com.radovan.play.services.EurekaRegistrationService;
import org.apache.pekko.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class EurekaRegistrationServiceImpl implements EurekaRegistrationService {

    private static final String EUREKA_SERVER_URL = "http://eureka-server:8761/eureka/apps";
    private final WSClient wsClient;
    private final ObjectMapper objectMapper;

    @Inject
    public EurekaRegistrationServiceImpl(WSClient wsClient, ObjectMapper objectMapper, ActorSystem actorSystem, ExecutionContext executionContext) {
        this.wsClient = wsClient;
        this.objectMapper = objectMapper;

        // Zakazivanje periodičnog izvršavanja registerService()
        actorSystem.scheduler().schedule(
                Duration.create(0, TimeUnit.SECONDS),  // Početno kašnjenje
                Duration.create(30, TimeUnit.SECONDS), // Period ponavljanja (30 sekundi)
                this::registerService,                 // Metoda koja se izvršava
                executionContext                       // Kontekst izvršavanja
        );
    }

    @Override
    public void registerService() {
        try {

            // Dinamičko dohvaćanje IP adrese i hostname-a
            String hostname = InetAddress.getLocalHost().getHostName();
            String ipAddr = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Hostname: " + hostname);
            System.out.println("IP Address: " + ipAddr);

            String appName = "product-service";
            String instanceId = appName + "-01";
            int port = 9000;

            // Kreiranje podataka za registraciju
            Map<String, Object> instanceData = new HashMap<>();
            instanceData.put("instanceId", instanceId);
            instanceData.put("app", appName);
            instanceData.put("hostName", hostname);
            instanceData.put("ipAddr", ipAddr);
            instanceData.put("statusPageUrl", "http://" + ipAddr + ":" + port + "/info");
            instanceData.put("healthCheckUrl", "http://" + ipAddr + ":" + port + "/health");
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

            // Slanje POST zahteva ka Eureka serveru
            String registrationUrl = EUREKA_SERVER_URL + "/" + appName;

            wsClient.url(registrationUrl)
                    .addHeader("Content-Type", "application/json")
                    .post(jsonPayload)
                    .thenAccept(response -> handleResponse(response));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to register service with Eureka", e);
        }
    }

    private void handleResponse(WSResponse response) {
        System.out.println("Eureka server response status: " + response.getStatus());
        if (response.getStatus() == 204 || response.getStatus() == 200) {
            System.out.println("Service registered successfully!");
        } else {
            System.err.println("Failed to register service with Eureka: " + response.getStatusText());
            System.err.println("Response body: " + response.getBody());
        }
    }
}
