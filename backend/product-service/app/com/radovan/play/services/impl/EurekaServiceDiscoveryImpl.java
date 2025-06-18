package com.radovan.play.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radovan.play.services.EurekaServiceDiscovery;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;

@Singleton
public class EurekaServiceDiscoveryImpl implements EurekaServiceDiscovery {

    private static final String EUREKA_API_SERVICES_URL = "http://eureka-server:8761/eureka/apps";
    private final WSClient wsClient;
    private final ObjectMapper objectMapper;

    @Inject
    public EurekaServiceDiscoveryImpl(WSClient wsClient) {
        this.wsClient = wsClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getServiceUrl(String serviceName) {
        try {
            String serviceUrl = EUREKA_API_SERVICES_URL + "/" + serviceName;
            System.out.println("*** Pokušavam da preuzmem servis: " + serviceName);
            System.out.println("*** URL za Eureka API: " + serviceUrl);

            WSResponse rawResponse = wsClient.url(serviceUrl)
                    .setRequestTimeout(5000)
                    .addHeader("Accept", "application/json")
                    .get()
                    .toCompletableFuture()
                    .join();

            System.out.println("*** WSResponse primljen! Status: " + rawResponse.getStatus());
            String rawBody = rawResponse.getBody();
            System.out.println("*** Odgovor od Eureka registry: " + rawBody);

            if (rawResponse.getStatus() != 200) {
                throw new RuntimeException("Eureka registry ne odgovara ispravno! Status: " + rawResponse.getStatus());
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
                    throw new RuntimeException("Nevalidni podaci za servis: " + serviceName);
                }

                return "http://" + address + ":" + port;
            }

            throw new RuntimeException("Servis nije pronađen: " + serviceName);

        } catch (Exception e) {
            System.err.println("*** Neuspešno preuzimanje URL-a iz Eureka registry! *** " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch service URL from Eureka registry", e);
        }
    }
}
