package com.radovan.play.utils;

import io.nats.client.Connection;
import io.nats.client.Nats;
import jakarta.inject.Singleton;

@Singleton
public class NatsUtils {

    private Connection nc;

    public NatsUtils() {
        try {
            this.nc = Nats.connect("nats://nats:4222");
            System.out.println("*** NATS connection has been established!");
        } catch (Exception e) {
            System.err.println("*** Error accessing NATS server!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return nc;
    }
}
