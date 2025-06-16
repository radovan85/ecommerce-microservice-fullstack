package com.radovan.play.security;

import com.radovan.play.utils.JwtUtil;
import jakarta.inject.Inject;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class JwtAuthAction extends Action.Simple {

    private final JwtUtil jwtUtil;

    @Inject
    public JwtAuthAction(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        String token = request.headers().get("Authorization").orElse(null);
        if (token == null || !token.startsWith("Bearer ")) {
            return CompletableFuture.completedFuture(unauthorized("Missing or invalid token"));
        }

        token = token.substring(7); // 🏗 Uklanjamo "Bearer " prefix


        return jwtUtil.extractUsername(token).thenCombine(jwtUtil.extractRoles(token), (userIdOpt, rolesOpt) -> {

            if (userIdOpt.isEmpty() || rolesOpt.isEmpty() || rolesOpt.get().isEmpty()) {
                return unauthorized("Invalid token");
            }

            // ✅ Kreiramo novu promenljivu jer `request` ne može biti direktno modifikovan u lambda izrazu
            Http.Request updatedRequest = request.addAttr(SecurityAttrs.USER_ID, userIdOpt.get())
                    .addAttr(SecurityAttrs.ROLES, rolesOpt.get());

            return delegate.call(updatedRequest).toCompletableFuture().join(); // ✅ Prosleđujemo izmenjen `request`
        });
    }
}