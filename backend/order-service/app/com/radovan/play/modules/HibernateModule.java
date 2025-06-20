package com.radovan.play.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.radovan.play.utils.HibernateUtil;
import jakarta.inject.Singleton;
import org.hibernate.SessionFactory;

public class HibernateModule extends AbstractModule {
    @Override
    protected void configure() {
        // Prazna konfiguracija, koristimo @Provides
    }

    @Provides
    @Singleton
    public SessionFactory provideSessionFactory() {
        HibernateUtil hibernateUtil = new HibernateUtil();
        return hibernateUtil.getSessionFactory();
    }
}
