package com.radovan.play.filters;

import com.radovan.play.security.SecurityAttrs;
import com.radovan.play.utils.JwtUtil;
import com.typesafe.config.Config;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.pekko.stream.Materializer;
import play.libs.Json;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Singleton
public class SecurityFilter extends Filter {

    private final JwtUtil jwtUtil;

    @Inject
    public SecurityFilter(Materializer mat, JwtUtil jwtUtil, Config config) {
        super(mat);
        this.jwtUtil = jwtUtil;
    }



    @Override
    public CompletionStage<Result> apply(
            Function<Http.RequestHeader, CompletionStage<Result>> next,
            Http.RequestHeader requestHeader
    ) {
        // 1. Preskoči filter za public rute
        if (isPublicRoute(requestHeader)) {
            return next.apply(requestHeader);
        }

        // 2. Proveri JWT token
        Optional<String> token = requestHeader.header("Authorization")
                .map(header -> header.startsWith("Bearer ") ? header.substring(7) : header);

        if (token.isEmpty()) {
            return unauthorizedResponse("Missing authorization token");
        }

        // 3. Verifikuj token i ekstrahuj podatke
        return jwtUtil.validateToken(token.get())
                .thenCompose(isValid -> {
                    if (!isValid) {
                        return unauthorizedResponse("Invalid token");
                    }

                    return jwtUtil.extractRoles(token.get())
                            .thenCompose(rolesOpt -> jwtUtil.extractUsername(token.get())
                                    .thenCompose(userIdOpt -> {
                                        if (userIdOpt.isEmpty()) {
                                            return unauthorizedResponse("Invalid user in token");
                                        }

                                        List<String> roles = rolesOpt.orElse(Collections.emptyList());
                                        String userId = userIdOpt.get();

                                        // 4. Kreiraj novi Request sa atributima
                                        Http.Request newRequest = new Http.RequestBuilder()
                                                .uri(requestHeader.uri())
                                                .method(requestHeader.method())
                                                .remoteAddress(requestHeader.remoteAddress())
                                                .headers(requestHeader.headers())
                                                .attrs(
                                                        requestHeader.attrs()
                                                                .put(SecurityAttrs.ROLES, roles)
                                                                .put(SecurityAttrs.USER_ID, userId)
                                                )
                                                .build();

                                        return next.apply(newRequest);
                                    })
                            );
                });
    }

    private CompletionStage<Result> unauthorizedResponse(String message) {
        return CompletableFuture.completedFuture(
                Results.forbidden(Json.toJson(Map.of(
                        "error", message,
                        "status", 403
                )))
        );
    }




    public List<String> getUnsecuredRoutes(){
        return List.of(
                "/api/health",
                "/prometheus"
        ); // for routes without security checking
    }

    private boolean isPublicRoute(Http.RequestHeader request) {
        return getUnsecuredRoutes().contains(request.path()) || request.method().equals("OPTIONS");
    }
}