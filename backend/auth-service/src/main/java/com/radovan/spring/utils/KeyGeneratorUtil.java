package com.radovan.spring.utils;

import java.security.*;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KeyGeneratorUtil {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        // Skladišti privatni ključ
        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----";
        Files.write(Paths.get("private_key.pem"), privateKeyPEM.getBytes());

        // Skladišti javni ključ
        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";
        Files.write(Paths.get("public_key.pem"), publicKeyPEM.getBytes());

        System.out.println("Ključevi generisani uspešno! 🚀");
    }
}

