package com.radovan.play.utils;

import com.radovan.play.exceptions.InstanceUndefinedException;
import play.mvc.Http;

public class TokenUtils {

    private TokenUtils() {

    }

    public static String provideToken(Http.Request request) {
        return request.header("Authorization")
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7).trim())
                .orElseThrow(() -> new InstanceUndefinedException("Missing or invalid authorization token"));
    }
}