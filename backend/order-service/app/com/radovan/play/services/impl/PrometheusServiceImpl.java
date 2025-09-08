package com.radovan.play.services.impl;

import com.radovan.play.services.PrometheusService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

@Singleton
public class PrometheusServiceImpl implements PrometheusService {

    // 🔧 System beans
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

    // 📈 Micrometer registry
    private MeterRegistry registry;

    // 📈 Counters and Timers
    private Counter requestCounter;
    private Counter dbQueryCounter;
    private Counter externalApiLatencyCounter;
    private Timer responseTimer;

    // 📊 Gauges (auto-updating via suppliers)
    private Gauge heapUsageGauge;
    private Gauge heapAllocationRateGauge;
    private Gauge activeThreadsGauge;
    private Gauge cpuLoadGauge;
    private Gauge activeSessionsGauge;

    /**
     * Idiomatic setter injection for full lifecycle-safe initialization.
     * Ensures registry is available before any metric registration.
     */
    @Inject
    public void setRegistry(MeterRegistry registry) {
        this.registry = registry;

        // 📈 Counters
        requestCounter = registry.counter("api_requests_total");
        dbQueryCounter = registry.counter("db_query_total");
        externalApiLatencyCounter = registry.counter("external_api_latency_seconds");
        responseTimer = registry.timer("response_time_seconds");

        // 📊 Gauges
        Gauge.builder("heap_used_bytes", memoryMXBean, bean -> (double) bean.getHeapMemoryUsage().getUsed())
                .register(registry);

        Gauge.builder("heap_allocation_rate", memoryMXBean, bean -> (double) bean.getHeapMemoryUsage().getCommitted())
                .register(registry);

        Gauge.builder("active_threads_total", threadMXBean, bean -> (double) bean.getThreadCount())
                .register(registry);

        Gauge.builder("cpu_load_percentage", osMXBean, bean -> bean.getSystemLoadAverage())
                .register(registry);

        Gauge.builder("active_sessions", threadMXBean, bean -> (double) bean.getPeakThreadCount())
                .register(registry);
    }

    // 🔁 Metric update methods

    @Override
    public void increaseRequestCount() {
        requestCounter.increment();
    }

    @Override
    public void recordResponseTime(double duration) {
        responseTimer.record((long) (duration * 1000), TimeUnit.MILLISECONDS);
    }

    @Override
    public void updateMemoryUsage() {
        // Gauge auto-updates via supplier; no manual trigger needed
    }

    @Override
    public void updateThreadCount() {
        // Gauge auto-updates via supplier; no manual trigger needed
    }

    @Override
    public void updateCpuLoad() {
        // Gauge auto-updates via supplier; no manual trigger needed
    }

    @Override
    public void updateDatabaseQueryCount() {
        dbQueryCounter.increment();
    }

    @Override
    public void updateHeapAllocationRate() {
        // Gauge auto-updates via supplier; no manual trigger needed
    }

    @Override
    public void updateActiveSessions() {
        // Gauge auto-updates via supplier; no manual trigger needed
    }

    @Override
    public void updateHttpStatusCount(int statusCode) {
        int statusClass = statusCode / 100;
        switch (statusClass) {
            case 2 -> registry.counter("http_requests_status_total", "status", "2xx").increment();
            case 4 -> registry.counter("http_requests_status_total", "status", "4xx").increment();
            case 5 -> registry.counter("http_requests_status_total", "status", "5xx").increment();
            default -> {
                // Ignore 1xx, 3xx, etc.
            }
        }
    }

    @Override
    public void updateExternalApiLatency(double duration) {
        externalApiLatencyCounter.increment(duration);
    }

    @Override
    public void updateControllerMethodTags(String controller, String method) {
        registry.counter("http_requests_by_controller_method", "controller", controller, "method", method).increment();
    }
}
