package com.radovan.play.security;

import play.mvc.With;
import java.lang.annotation.*;

@With(RoleSecuredAction.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RoleSecured {
    String[] value();
}