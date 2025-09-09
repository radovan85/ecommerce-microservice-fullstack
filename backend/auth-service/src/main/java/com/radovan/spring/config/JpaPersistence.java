package com.radovan.spring.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
public class JpaPersistence {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(getHikariDataSource());
        em.setPackagesToScan(new String[] { "com.radovan.spring.entity" });

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        // Postavljanje interfejsa EntityManagerFactory
        em.setEntityManagerFactoryInterface(EntityManagerFactory.class);

        return em;
    }


    @Bean
    public HikariDataSource getHikariDataSource() {
        HikariDataSource hikariConfig = new HikariDataSource();
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        String dbUrl = System.getenv("DB_URL");
        String dbPassword = System.getenv("DB_PASSWORD");
        String dbUsername = System.getenv("DB_USERNAME");
        if (dbUrl == null || dbUsername == null || dbPassword == null) throw new IllegalStateException("Database environment variables are missing!");
        hikariConfig.setJdbcUrl(dbUrl);
        hikariConfig.setUsername(dbUsername);
        hikariConfig.setPassword(dbPassword);
        return hikariConfig;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update"); // Kontrola kreiranja šeme
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"); // Dialekt baze
        return properties;
    }
}