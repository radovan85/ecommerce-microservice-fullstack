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
@RequestMapping("/prometheus")
public class PrometheusController {

    private PrometheusMeterRegistry prometheusRegistry;
    private RestTemplate restTemplate;
    private ServiceUrlProvider urlProvider;


    @Autowired
    private void initialize(PrometheusMeterRegistry prometheusRegistry,
                            RestTemplate restTemplate, ServiceUrlProvider urlProvider) {
        this.prometheusRegistry = prometheusRegistry;
        this.restTemplate = restTemplate;
        this.urlProvider = urlProvider;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> scrape() {
        return new ResponseEntity<>(prometheusRegistry.scrape(), HttpStatus.OK);
    }


    @GetMapping(value = "/metrics", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> forwardAllMetrics() {
        StringBuilder allMetrics = new StringBuilder();
        String enpointSuffix = "/prometheus";
        String[] services = {
                urlProvider.getAuthServiceUrl(),
                urlProvider.getCartServiceUrl(),
                urlProvider.getCustomerServiceUrl(),
                urlProvider.getOrderServiceUrl(),
                urlProvider.getProductServiceUrl(),
                urlProvider.getGatewayServiceUrl()
        };

        for (String url : services) {
            url = url + enpointSuffix;
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