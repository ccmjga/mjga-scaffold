package com.zl.mjga.platform.events;

import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/** Isolates event recovery behind an authenticated operator-only management boundary. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "mjga.events.management-enabled", havingValue = "true")
class EventPublicationSecurityConfiguration {

  @Bean
  InMemoryUserDetailsManager eventPublicationOperators(EventPublicationProperties properties) {
    return new InMemoryUserDetailsManager(
        User.withUsername(Objects.requireNonNull(properties.managementUsername()))
            .password("{noop}" + Objects.requireNonNull(properties.managementPassword()))
            .authorities("EVENT_PUBLICATION_OPERATOR")
            .build());
  }

  @Bean
  SecurityFilterChain eventPublicationSecurity(HttpSecurity http) throws Exception {
    return http.securityMatcher(EndpointRequest.to(EventPublicationEndpoint.class))
        .authorizeHttpRequests(
            requests -> requests.anyRequest().hasAuthority("EVENT_PUBLICATION_OPERATOR"))
        .httpBasic(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }
}
