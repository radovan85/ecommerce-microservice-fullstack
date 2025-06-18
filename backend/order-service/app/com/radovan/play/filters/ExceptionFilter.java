package com.radovan.play.filters;

import com.radovan.play.exceptions.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.pekko.stream.Materializer;
import play.libs.Json;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Singleton
public class ExceptionFilter extends Filter {

    @Inject
    public ExceptionFilter(Materializer mat) {
        super(mat);
    }

    @Override
    public CompletionStage<Result> apply(
            Function<Http.RequestHeader, CompletionStage<Result>> next,
            Http.RequestHeader requestHeader
    ) {
        return next.apply(requestHeader).handle((result, throwable) -> {
            if (throwable != null) {
                // Otpakuj CompletionException
                Throwable realException = throwable;
                while (realException instanceof CompletionException && realException.getCause() != null) {
                    realException = realException.getCause();
                }

                if (realException instanceof DataNotValidatedException) {
                    return Results.status(406, Json.toJson(realException.getMessage()));
                }

                if (realException instanceof InvalidCartException) {
                    return Results.status(406, Json.toJson(realException.getMessage()));
                }

                if (realException instanceof InstanceUndefinedException) {
                    return Results.status(422, Json.toJson(realException.getMessage()));
                }
                if (realException instanceof ForbiddenAccessException) {
                    return Results.status(403, Json.toJson(realException.getMessage()));
                }
                if(realException instanceof OutOfStockException){
                    return Results.status(406, Json.toJson(realException.getMessage()));
                }

                return Results.internalServerError(Json.toJson("Server error: " + realException.getMessage()));
            }
            return result;
        });
    }
}
