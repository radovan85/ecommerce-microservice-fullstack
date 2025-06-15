package com.radovan.play.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import jakarta.inject.Singleton;

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

            // Postavi Hibernate properties
            configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/ecommerce-db");
            configuration.setProperty("hibernate.connection.username", "postgres");
            configuration.setProperty("hibernate.connection.password", "1111");
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");
            configuration.setProperty("hibernate.show_sql", "false");
            configuration.setProperty("hibernate.format_sql", "false");

            // Dodaj anotirane klase
            configuration.addAnnotatedClass(com.radovan.play.entity.ProductEntity.class);
            configuration.addAnnotatedClass(com.radovan.play.entity.ProductCategoryEntity.class);
            configuration.addAnnotatedClass(com.radovan.play.entity.ProductImageEntity.class);

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
