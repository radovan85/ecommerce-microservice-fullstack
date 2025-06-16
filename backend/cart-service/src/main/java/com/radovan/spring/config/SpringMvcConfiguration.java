package com.radovan.spring.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableScheduling
@EnableWebMvc
@ComponentScan(basePackages = "com.radovan.spring")
public class SpringMvcConfiguration implements WebMvcConfigurer {

	@Bean
	public ModelMapper getMapper() {
		ModelMapper returnValue = new ModelMapper();
		returnValue.getConfiguration().setAmbiguityIgnored(true).setFieldAccessLevel(AccessLevel.PRIVATE);
		returnValue.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return returnValue;
	}

	@Bean
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}
