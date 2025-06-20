package com.radovan.play.config;

import com.radovan.play.services.EurekaRegistrationService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class EurekaClientRegistrationInitializer {

    private EurekaRegistrationService eurekaRegistrationService;

    @Inject
    private void initialize(EurekaRegistrationService eurekaRegistrationService) {
        this.eurekaRegistrationService = eurekaRegistrationService;
        System.out.println("EurekaClientRegistrationInitializer created.");
        initialize();
    }

    private void initialize() {
        try {
            System.out.println("Initializing Eureka registration...");
            eurekaRegistrationService.registerService();
        } catch (Exception e) {
            System.out.println("Error during service registration: " + e.getMessage());
            e.printStackTrace();

            // Retry logic in case of failure
            retryRegistration(3, 5000); // 3 attempts, 5 seconds delay
        }
    }

    private void retryRegistration(int retries, long delayInMillis) {
        for (int i = 0; i < retries; i++) {
            try {
                System.out.println("Retrying registration (" + (i + 1) + "/" + retries + ")...");
                eurekaRegistrationService.registerService();
                return; // Exit loop if registration succeeds
            } catch (Exception e) {
                System.out.println("Retry failed. Waiting...");
                try {
                    Thread.sleep(delayInMillis);
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new RuntimeException("Failed to register service after " + retries + " attempts.");
    }
}
