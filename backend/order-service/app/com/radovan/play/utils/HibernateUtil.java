package com.radovan.play.utils;

import jakarta.inject.Singleton;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

@Singleton
public class HibernateUtil {
    private final SessionFactory sessionFactory;

    public HibernateUtil() {
        this.sessionFactory = buildSessionFactory();
    }

    private SessionFactory buildSessionFactory() {
        try {
            // Kreiraj Configuration instancu
            Configuration configuration = new Configuration();

            // Postavi Hibernate properties iz ENV varijabli
            configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            configuration.setProperty("hibernate.connection.url", System.getenv("DB_URL"));
            configuration.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"));
            configuration.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"));
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");
            configuration.setProperty("hibernate.show_sql", "false");
            configuration.setProperty("hibernate.format_sql", "false");

            // Dodaj anotirane klase
            configuration.addAnnotatedClass(com.radovan.play.entity.OrderEntity.class);
            configuration.addAnnotatedClass(com.radovan.play.entity.OrderAddressEntity.class);
            configuration.addAnnotatedClass(com.radovan.play.entity.OrderItemEntity.class);

            // Napravi SessionFactory iz service registry
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            return configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}