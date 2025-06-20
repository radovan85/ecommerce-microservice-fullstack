package com.radovan.play.modules;

import com.google.inject.AbstractModule;
import com.radovan.play.brokers.OrderNatsListener;
import com.radovan.play.brokers.OrderNatsSender;
import com.radovan.play.config.EurekaClientRegistrationInitializer;
import com.radovan.play.converter.TempConverter;
import com.radovan.play.repositories.OrderAddressRepository;
import com.radovan.play.repositories.OrderItemRepository;
import com.radovan.play.repositories.OrderRepository;
import com.radovan.play.repositories.impl.OrderAddressRepositoryImpl;
import com.radovan.play.repositories.impl.OrderItemRepositoryImpl;
import com.radovan.play.repositories.impl.OrderRepositoryImpl;
import com.radovan.play.services.*;
import com.radovan.play.services.impl.*;
import com.radovan.play.utils.JwtUtil;
import com.radovan.play.utils.NatsUtils;
import com.radovan.play.utils.PublicKeyCache;
import com.radovan.play.utils.ServiceUrlProvider;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Environment;

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
        bind(OrderService.class).to(OrderServiceImpl.class).asEagerSingleton();
        bind(OrderRepository.class).to(OrderRepositoryImpl.class).asEagerSingleton();
        bind(OrderItemService.class).to(OrderItemServiceImpl.class).asEagerSingleton();
        bind(OrderItemRepository.class).to(OrderItemRepositoryImpl.class).asEagerSingleton();
        bind(OrderAddressService.class).to(OrderAddressServiceImpl.class).asEagerSingleton();
        bind(OrderAddressRepository.class).to(OrderAddressRepositoryImpl.class).asEagerSingleton();
        bind(EurekaRegistrationService.class).to(EurekaRegistrationServiceImpl.class).asEagerSingleton();
        bind(EurekaServiceDiscovery.class).to(EurekaServiceDiscoveryImpl.class).asEagerSingleton();
        bind(EurekaClientRegistrationInitializer.class).asEagerSingleton();
        bind(ServiceUrlProvider.class).asEagerSingleton();
        bind(PublicKeyCache.class).asEagerSingleton();
        bind(JwtUtil.class).asEagerSingleton();
        bind(TempConverter.class).asEagerSingleton();
        bind(OrderNatsSender.class).asEagerSingleton();
        bind(OrderNatsListener.class).asEagerSingleton();
        bind(NatsUtils.class).asEagerSingleton();

    }
}