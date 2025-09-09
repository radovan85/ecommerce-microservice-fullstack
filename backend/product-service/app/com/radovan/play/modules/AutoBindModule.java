package com.radovan.play.modules;

import com.google.inject.AbstractModule;
import com.radovan.play.brokers.ProductNatsListener;
import com.radovan.play.brokers.ProductNatsSender;
import com.radovan.play.converter.TempConverter;
import com.radovan.play.repositories.ProductCategoryRepository;
import com.radovan.play.repositories.ProductImageRepository;
import com.radovan.play.repositories.ProductRepository;
import com.radovan.play.repositories.impl.ProductCategoryRepositoryImpl;
import com.radovan.play.repositories.impl.ProductImageRepositoryImpl;
import com.radovan.play.repositories.impl.ProductRepositoryImpl;
import com.radovan.play.services.EurekaRegistrationService;
import com.radovan.play.services.EurekaServiceDiscovery;
import com.radovan.play.services.ProductCategoryService;
import com.radovan.play.services.ProductImageService;
import com.radovan.play.services.ProductService;
import com.radovan.play.services.PrometheusService;
import com.radovan.play.services.impl.EurekaRegistrationServiceImpl;
import com.radovan.play.services.impl.EurekaServiceDiscoveryImpl;
import com.radovan.play.services.impl.ProductCategoryServiceImpl;
import com.radovan.play.services.impl.ProductImageServiceImpl;
import com.radovan.play.services.impl.ProductServiceImpl;
import com.radovan.play.services.impl.PrometheusServiceImpl;
import com.radovan.play.utils.JwtUtil;
import com.radovan.play.utils.NatsUtils;
import com.radovan.play.utils.PublicKeyCache;
import com.radovan.play.utils.ServiceUrlProvider;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

public class AutoBindModule extends AbstractModule {

    @Override
    protected void configure() {
        // Bind services to their implementations
        bind(ProductCategoryService.class).to(ProductCategoryServiceImpl.class).asEagerSingleton();
        bind(ProductImageService.class).to(ProductImageServiceImpl.class).asEagerSingleton();
        bind(ProductService.class).to(ProductServiceImpl.class).asEagerSingleton();
        bind(PrometheusService.class).to(PrometheusServiceImpl.class).asEagerSingleton();
        bind(EurekaRegistrationService.class).to(EurekaRegistrationServiceImpl.class).asEagerSingleton();
        bind(EurekaServiceDiscovery.class).to(EurekaServiceDiscoveryImpl.class).asEagerSingleton();

        // Bind repositories to their implementations
        bind(ProductCategoryRepository.class).to(ProductCategoryRepositoryImpl.class).asEagerSingleton();
        bind(ProductImageRepository.class).to(ProductImageRepositoryImpl.class).asEagerSingleton();
        bind(ProductRepository.class).to(ProductRepositoryImpl.class).asEagerSingleton();

        // Bind utility classes as eager singletons
        bind(TempConverter.class).asEagerSingleton();
        bind(JwtUtil.class).asEagerSingleton();
        bind(NatsUtils.class).asEagerSingleton();
        bind(PublicKeyCache.class).asEagerSingleton();
        bind(ServiceUrlProvider.class).asEagerSingleton();

        // Bind NATS components
        bind(ProductNatsSender.class).asEagerSingleton();
        bind(ProductNatsListener.class).asEagerSingleton();

        // Create and bind Prometheus registry instances
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        bind(PrometheusMeterRegistry.class).toInstance(prometheusRegistry);
        bind(MeterRegistry.class).toInstance(prometheusRegistry);
    }
}