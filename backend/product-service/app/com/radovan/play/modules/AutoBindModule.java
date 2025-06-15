package com.radovan.play.modules;

import com.google.inject.AbstractModule;
import com.radovan.play.brokers.ProductNatsListener;
import com.radovan.play.brokers.ProductNatsSender;
import com.radovan.play.config.EurekaClientRegistrationInitializer;
import com.radovan.play.converter.TempConverter;
import com.radovan.play.repositories.ProductCategoryRepository;
import com.radovan.play.repositories.ProductImageRepository;
import com.radovan.play.repositories.ProductRepository;
import com.radovan.play.repositories.impl.ProductCategoryRepositoryImpl;
import com.radovan.play.repositories.impl.ProductImageRepositoryImpl;
import com.radovan.play.repositories.impl.ProductRepositoryImpl;
import com.radovan.play.services.*;
import com.radovan.play.services.impl.*;
import com.radovan.play.utils.*;
import play.Environment;
import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoBindModule extends AbstractModule {

    private final Environment environment;
    private final Config config;
    private static final Logger logger = LoggerFactory.getLogger(AutoBindModule.class);

    public AutoBindModule(Environment environment, Config config) {
        this.environment = environment;
        this.config = config;
    }

    @Override
    protected void configure() {
        bind(ProductCategoryService.class).to(ProductCategoryServiceImpl.class).asEagerSingleton();
        bind(ProductCategoryRepository.class).to(ProductCategoryRepositoryImpl.class).asEagerSingleton();
        bind(ProductService.class).to(ProductServiceImpl.class).asEagerSingleton();
        bind(ProductRepository.class).to(ProductRepositoryImpl.class).asEagerSingleton();
        bind(ProductImageService.class).to(ProductImageServiceImpl.class).asEagerSingleton();
        bind(ProductImageRepository.class).to(ProductImageRepositoryImpl.class).asEagerSingleton();
        bind(EurekaRegistrationService.class).to(EurekaRegistrationServiceImpl.class).asEagerSingleton();
        bind(EurekaServiceDiscovery.class).to(EurekaServiceDiscoveryImpl.class).asEagerSingleton();
        bind(EurekaClientRegistrationInitializer.class).asEagerSingleton();
        bind(ServiceUrlProvider.class).asEagerSingleton();
        bind(PublicKeyCache.class).asEagerSingleton();
        bind(JwtUtil.class).asEagerSingleton();
        bind(TempConverter.class).asEagerSingleton();
        bind(ProductNatsListener.class).asEagerSingleton();
        bind(ProductNatsSender.class).asEagerSingleton();
        bind(NatsUtils.class).asEagerSingleton();
    }
}