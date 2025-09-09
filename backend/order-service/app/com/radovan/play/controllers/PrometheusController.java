package com.radovan.play.controllers;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;

public class PrometheusController extends Controller {

    private PrometheusMeterRegistry registry;

    @Inject
    private void initialize(PrometheusMeterRegistry registry){
        this.registry = registry;
    }

    public Result getMetrics(){
        return ok(registry.scrape());
    }
}
