package com.radovan.spring.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.radovan.spring.services.PrometheusService;
import com.radovan.spring.utils.ServiceUrlProvider;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

@RestController
@RequestMapping("/actuator")
public class PrometheusController {

	private PrometheusService prometheusService;
	private PrometheusMeterRegistry prometheusRegistry;
	private RestTemplate restTemplate;
	private ServiceUrlProvider urlProvider;

	
	@Autowired
	private void initialize(PrometheusService prometheusService, PrometheusMeterRegistry prometheusRegistry,
			RestTemplate restTemplate, ServiceUrlProvider urlProvider) {
		this.prometheusService = prometheusService;
		this.prometheusRegistry = prometheusRegistry;
		this.restTemplate = restTemplate;
		this.urlProvider = urlProvider;
	}

	@GetMapping(value = "/prometheus", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> scrape() {
		return new ResponseEntity<>(prometheusRegistry.scrape(), HttpStatus.OK);
	}

	@GetMapping("/test")
	public ResponseEntity<String> test() {
		prometheusService.increaseRequestCount();
		return new ResponseEntity<>("Counter increased!", HttpStatus.OK);
	}

	@GetMapping("/memory")
	public ResponseEntity<String> memoryUsage() {
		prometheusService.updateMemoryUsage();
		return new ResponseEntity<>("Heap memory updated!", HttpStatus.OK);
	}

	@GetMapping("/threads")
	public ResponseEntity<String> threadCount() {
		prometheusService.updateThreadCount();
		return new ResponseEntity<>("Thread count updated!", HttpStatus.OK);
	}

	@GetMapping("/cpu")
	public ResponseEntity<String> cpuLoad() {
		prometheusService.updateCpuLoad();
		return new ResponseEntity<>("CPU load updated!", HttpStatus.OK);
	}

	@GetMapping("/response-time")
	public ResponseEntity<String> responseTime() {
		prometheusService.recordResponseTime(0.1); // Simulirani podatak
		return new ResponseEntity<>("Response time metric recorded!", HttpStatus.OK);
	}

	@GetMapping("/db-queries")
	public ResponseEntity<String> databaseQueries() {
		prometheusService.updateDatabaseQueryCount();
		return new ResponseEntity<>("Database query metric updated!", HttpStatus.OK);
	}

	@GetMapping("/heap-allocation")
	public ResponseEntity<String> heapAllocationRate() {
		prometheusService.updateHeapAllocationRate();
		return new ResponseEntity<>("Heap allocation rate updated!", HttpStatus.OK);
	}

	@GetMapping("/active-sessions")
	public ResponseEntity<String> activeSessions() {
		prometheusService.updateActiveSessions();
		return new ResponseEntity<>("Active sessions metric updated!", HttpStatus.OK);
	}

	@GetMapping("/http-status")
	public ResponseEntity<String> httpStatusCount() {
		prometheusService.updateHttpStatusCount(200); // Simulirani podatak
		return new ResponseEntity<>("HTTP status metric recorded!", HttpStatus.OK);
	}

	@GetMapping("/external-api")
	public ResponseEntity<String> externalApiLatency() {
		prometheusService.updateExternalApiLatency(0.5); // Simulirani podatak
		return new ResponseEntity<>("External API latency metric recorded!", HttpStatus.OK);
	}
	
	@GetMapping(value = "/metrics/all", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> forwardAllMetrics() {
		StringBuilder allMetrics = new StringBuilder();

		String[] services = { 
				urlProvider.getAuthServiceUrl() + "/metrics",
				urlProvider.getCartServiceUrl() + "/metrics",
				urlProvider.getCustomerServiceUrl() + "/metrics",
				urlProvider.getOrderServiceUrl() + "/metrics",
				urlProvider.getProductServiceUrl() + "/metrics"
				};

		for (String url : services) {
			try {
				String data = restTemplate.getForObject(url, String.class);
				allMetrics.append(data).append("\n");
			} catch (Exception e) {
				allMetrics.append("# Failed to fetch metrics from ").append(url).append("\n");
			}
		}

		return ResponseEntity.ok(allMetrics.toString());
	}
}