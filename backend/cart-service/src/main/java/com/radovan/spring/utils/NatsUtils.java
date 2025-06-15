package com.radovan.spring.utils;

import org.springframework.stereotype.Component;

import io.nats.client.Connection;
import io.nats.client.Nats;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class NatsUtils {

    private Connection nc;

    @PostConstruct
    public void init() {
        try {
            this.nc = Nats.connect("nats://localhost:4222");
            System.out.println("*** NATS connection has been established!");
        } catch (Exception e) {
            System.err.println("*** Error accessing NATS server!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return nc;
    }

    @PreDestroy
    public void closeConnection() {
        try {
            if (nc != null) {
                nc.close();
                System.out.println("*** NATS connection closed!");
            }
        } catch (Exception e) {
            System.err.println("*** Error closing NATS connection!");
            e.printStackTrace();
        }
    }
}

