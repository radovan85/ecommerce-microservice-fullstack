package com.radovan.spring.services.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.radovan.spring.services.PrometheusService;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.OperatingSystemMXBean;

@Service
public class PrometheusServiceImpl implements PrometheusService {

    private Counter requestCounter;
    private Counter dbQueryCounter;
    private Counter httpStatusCounter200;
    private Counter httpStatusCounter400;
    private Counter httpStatusCounter500;
    private Counter externalApiLatencyCounter;
    private Timer responseTimer;
    private Gauge heapUsageGauge;
    private Gauge heapAllocationRateGauge;
    private Gauge activeThreadsGauge;
    private Gauge cpuLoadGauge;
    private Gauge activeSessionsGauge;

    @Autowired
    private void initialize(MeterRegistry registry) {
        this.requestCounter = registry.counter("api_requests_total");
        this.dbQueryCounter = registry.counter("db_query_total");
        this.httpStatusCounter200 = registry.counter("http_requests_status_total", "status", "200");
        this.httpStatusCounter400 = registry.counter("http_requests_status_total", "status", "400");
        this.httpStatusCounter500 = registry.counter("http_requests_status_total", "status", "500");
        this.externalApiLatencyCounter = registry.counter("external_api_latency_seconds");
        this.responseTimer = registry.timer("response_time_seconds");

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();

        this.heapUsageGauge = Gauge.builder("heap_used_bytes", memoryMXBean,
                bean -> bean.getHeapMemoryUsage().getUsed()).register(registry);

        this.heapAllocationRateGauge = Gauge.builder("heap_allocation_rate", memoryMXBean,
                bean -> bean.getHeapMemoryUsage().getCommitted()).register(registry);

        this.activeThreadsGauge = Gauge.builder("active_threads_total", threadMXBean,
                ThreadMXBean::getThreadCount).register(registry);

        this.cpuLoadGauge = Gauge.builder("cpu_load_percentage", osMxBean,
                OperatingSystemMXBean::getSystemLoadAverage).register(registry);

        this.activeSessionsGauge = Gauge.builder("active_sessions", threadMXBean,
                ThreadMXBean::getPeakThreadCount).register(registry);
    }

    @Override
    public void increaseRequestCount() {
        requestCounter.increment();
    }

    @Override
    public void recordResponseTime(double duration) {
        responseTimer.record((long) (duration * 1000), java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Override
    @Scheduled(fixedRate = 5000)
    public void updateMemoryUsage() {
        heapUsageGauge.value();
    }

    @Override
    @Scheduled(fixedRate = 5000)
    public void updateThreadCount() {
        activeThreadsGauge.value();
    }

    @Override
    @Scheduled(fixedRate = 5000)
    public void updateCpuLoad() {
        cpuLoadGauge.value();
    }

    @Override
    public void updateDatabaseQueryCount() {
        dbQueryCounter.increment();
    }

    @Override
    public void updateHeapAllocationRate() {
        heapAllocationRateGauge.value();
    }

    @Override
    public void updateActiveSessions() {
        activeSessionsGauge.value();
    }

    @Override
    public void updateHttpStatusCount(int statusCode) {
        switch (statusCode) {
            case 200 -> httpStatusCounter200.increment();
            case 400 -> httpStatusCounter400.increment();
            case 500 -> httpStatusCounter500.increment();
        }
    }

    @Override
    public void updateExternalApiLatency(double duration) {
        externalApiLatencyCounter.increment(duration);
    }
}
