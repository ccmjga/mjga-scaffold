package com.zl.mjga.user.web;

import org.babyfish.jimmer.jackson.ImmutableModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonSerializeConfig {

  @ConditionalOnMissingBean(ImmutableModule.class)
  @Bean
  public ImmutableModule immutableModule() {
    return new ImmutableModule();
  }
}
