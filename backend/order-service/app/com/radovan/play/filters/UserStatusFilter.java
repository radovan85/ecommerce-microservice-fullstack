package com.radovan.play.filters;

import com.radovan.play.utils.ServiceUrlProvider;
import jakarta.inject.Inject;
import org.apache.pekko.stream.Materializer;
import play.libs.ws.WSClient;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class UserStatusFilter extends Filter {

    private final WSClient wsClient;
    private final ServiceUrlProvider urlProvider;

    @Inject
    public UserStatusFilter(Materializer mat, WSClient wsClient, ServiceUrlProvider urlProvider) {
        super(mat);
        this.wsClient = wsClient;
        this.urlProvider = urlProvider;
    }

    @Override
    public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> nextFilter, Http.RequestHeader request) {

        String authToken = request.headers().get("Authorization").orElse(null);

        if (authToken == null || !authToken.startsWith("Bearer ")) {
            return nextFilter.apply(request);
        }

        return wsClient.url(urlProvider.getAuthServiceUrl() + "/api/auth/me")
                .addHeader("Authorization", authToken)
                .get()
                .thenCompose(response -> {
                    String responseBody = response.getBody();

                    if (response.getStatus() == 451 || responseBody.contains("Account suspended")) {
                        return CompletableFuture.completedFuture(Results.status(451, "Account access restricted"));
                    }

                    return nextFilter.apply(request);
                })
                .exceptionally(ex -> {
                    return (Result) nextFilter.apply(request);
                });
    }


}
