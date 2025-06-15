package com.radovan.spring.utils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class KeyUtil {

    public static PublicKey getPublicKeyFromPrivate(PrivateKey privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(privateKey.getEncoded());
        return keyFactory.generatePublic(spec);
    }
}
