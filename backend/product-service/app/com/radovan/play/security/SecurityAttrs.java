package com.radovan.play.security;

import play.libs.typedmap.TypedKey;

import java.util.List;

public class SecurityAttrs {
    public static final TypedKey<String> USER_ID = TypedKey.create("userId");
    public static final TypedKey<List<String>> ROLES = TypedKey.create("roles");
}
