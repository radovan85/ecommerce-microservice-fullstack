package com.radovan.spring.controllers;


import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/prometheus")
public class PrometheusController {

    private PrometheusMeterRegistry registry;

    @Autowired
    private void initialize(PrometheusMeterRegistry registry){
        this.registry = registry;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getMetrics(){
        return new ResponseEntity<>(registry.scrape(), HttpStatus.OK);
    }

}
