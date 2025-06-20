package com.radovan.spring.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radovan.spring.services.EurekaRegistrationService;

@Component
public class EurekaClientRegistrationInitializer {

	@Autowired
	private EurekaRegistrationService eurekaRegistrationService;

	@PostConstruct
	public void initialize() {
		try {
			eurekaRegistrationService.registerService();
		} catch (Exception e) {
			System.out.println("Error during service registration: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
