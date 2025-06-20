package com.radovan.play.security;

import jakarta.inject.Inject;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public class RoleSecuredAction extends Action<RoleSecured> {

    @Inject
    public RoleSecuredAction() {}

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        List<String> userRoles = request.attrs().get(SecurityAttrs.ROLES);

        if (userRoles == null || userRoles.isEmpty()) {
            return delegate.call(request).thenApply(result -> forbidden("Access denied: No roles assigned"));
        }

        Set<String> requiredRoles = Set.of(configuration.value());
        boolean hasRole = userRoles.stream().anyMatch(requiredRoles::contains);

        if (!hasRole) {
            return delegate.call(request).thenApply(result -> forbidden("Access denied: Insufficient role"));
        }

        return delegate.call(request);
    }
}
