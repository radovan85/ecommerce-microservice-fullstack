package com.radovan.spring.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.radovan.spring.services.PrometheusService;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

@RestController
@RequestMapping("/actuator")
public class PrometheusController {

	private PrometheusService prometheusService;
	private PrometheusMeterRegistry prometheusRegistry;

	@Autowired
	private void initialize(PrometheusService prometheusService, PrometheusMeterRegistry prometheusRegistry) {
		this.prometheusService = prometheusService;
		this.prometheusRegistry = prometheusRegistry;
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
}