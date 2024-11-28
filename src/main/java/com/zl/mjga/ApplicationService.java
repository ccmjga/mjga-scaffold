package com.zl.mjga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.zl.mjga", "org.jooq.generated"})
public class ApplicationService {

  public static void main(String[] args) {
    SpringApplication.run(ApplicationService.class, args);
  }
}
