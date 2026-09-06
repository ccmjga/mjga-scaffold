package com.zl.mjga.platform.status;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
class PlatformStatusSecurityConfiguration {
  @Bean
  @Order(0)
  SecurityFilterChain platformStatusSecurity(HttpSecurity http) throws Exception {
    return http.securityMatcher("/api/v1/platform/status")
        .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())
        .build();
  }
}
