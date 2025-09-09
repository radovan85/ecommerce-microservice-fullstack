package com.radovan.play.filters;

import com.radovan.play.services.PrometheusService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.pekko.util.ByteString;
import play.libs.streams.Accumulator;
import play.mvc.EssentialAction;
import play.mvc.EssentialFilter;
import play.mvc.Result;

import java.util.List;
import java.util.concurrent.Executor;

@Singleton
public class UnifiedMetricsFilter extends EssentialFilter {

    private PrometheusService prometheus;
    private Executor executor;

    // Lista endpoint-a koji treba da budu izuzeti iz metrika
    private final List<String> excludedPaths = List.of(
            "/prometheus"
    );

    @Inject
    public void initialize(PrometheusService prometheus, Executor executor) {
        this.prometheus = prometheus;
        this.executor = executor;
    }

    @Override
    public EssentialAction apply(EssentialAction next) {
        return EssentialAction.of(request -> {
            // Proveri da li je putanja izuzeta
            if (isExcludedPath(request.path())) {
                return next.apply(request); // Preskoči metrike za ovaj endpoint
            }

            long startTimeNs = System.nanoTime();

            // 📈 Increase request count immediately
            prometheus.increaseRequestCount();

            Accumulator<ByteString, Result> accumulator = next.apply(request);

            return accumulator.map(result -> {
                double durationSec = (System.nanoTime() - startTimeNs) / 1_000_000_000.0;

                // ⏱ Record response time
                prometheus.recordResponseTime(durationSec);

                // 📊 Status classification (2xx, 4xx, 5xx)
                prometheus.updateHttpStatusCount(result.status());

                return result;
            }, executor);
        });
    }

    /**
     * Proverava da li putanja treba da bude izuzeta iz metrika
     */
    private boolean isExcludedPath(String path) {
        return excludedPaths.stream()
                .anyMatch(excludedPath ->
                        path.equals(excludedPath) ||
                                path.startsWith(excludedPath + "/"));
    }
}