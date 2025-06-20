package com.radovan.play.modules;

import com.google.inject.AbstractModule;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;

public class MapperModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ModelMapper.class).toInstance(getMapper());
    }

    public ModelMapper getMapper() {
        ModelMapper returnValue = new ModelMapper();
        returnValue.getConfiguration().setAmbiguityIgnored(true).setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
        returnValue.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return returnValue;
    }
}