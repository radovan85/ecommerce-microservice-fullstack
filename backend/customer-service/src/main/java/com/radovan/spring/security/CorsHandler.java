package com.radovan.spring.security;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CorsHandler implements CorsConfigurationSource {

	@Override
	public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
		// TODO Auto-generated method stub
		CorsConfiguration returnValue = new CorsConfiguration();
		returnValue.setAllowedOriginPatterns(Arrays.asList("*"));
		returnValue.setAllowCredentials(true);
		returnValue.setAllowedHeaders(Arrays.asList("Access-Control-Allow-Headers", "Access-Control-Allow-Origin",
				"Access-Control-Request-Method", "Access-Control-Request-Headers", "Origin", "Cache-Control",
				"Content-Type", "Authorization"));
		returnValue.setAllowedMethods(Arrays.asList("DELETE", "GET", "POST", "PATCH", "PUT"));
		return returnValue;
	}

}