package com.zl.mjga.common.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
public class HttpFireWallConfig {

  @Bean
  public HttpFirewall getHttpFirewall() {
    return new StrictHttpFirewall();
  }

  @Bean
  public RequestRejectedHandler requestRejectedHandler() {
    return (request, response, requestRejectedException) -> {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType(MediaType.TEXT_PLAIN_VALUE);
      try (PrintWriter writer = response.getWriter()) {
        writer.write(requestRejectedException.getMessage());
      }
    };
  }
}
