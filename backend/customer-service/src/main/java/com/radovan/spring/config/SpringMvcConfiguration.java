package com.radovan.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.radovan.spring.interceptors.AuthInterceptor;
import com.radovan.spring.interceptors.UnifiedMetricsInterceptor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = "com.radovan.spring")
@EnableWebMvc
public class SpringMvcConfiguration implements WebMvcConfigurer {

	private  AuthInterceptor authInterceptor;
	private  UnifiedMetricsInterceptor metricsInterceptor;

	@Autowired
	private void initialize(AuthInterceptor authInterceptor, UnifiedMetricsInterceptor metricsInterceptor) {
		this.authInterceptor = authInterceptor;
		this.metricsInterceptor = metricsInterceptor;
	}

	@Bean
	public ModelMapper getMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration()
				.setAmbiguityIgnored(true)
				.setFieldAccessLevel(AccessLevel.PRIVATE)
				.setMatchingStrategy(MatchingStrategies.STRICT);
		return modelMapper;
	}

	@Bean
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	@Primary
	public PrometheusMeterRegistry prometheusMeterRegistry() {
		return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
	}

	@Bean
	public MeterRegistry meterRegistry(PrometheusMeterRegistry prometheusRegistry) {
		return prometheusRegistry;
	}



	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
				.excludePathPatterns("/prometheus");
		registry.addInterceptor(metricsInterceptor)
				.excludePathPatterns("/prometheus");
	}
}
