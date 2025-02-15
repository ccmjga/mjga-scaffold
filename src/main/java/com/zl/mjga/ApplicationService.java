package com.zl.mjga;

import org.babyfish.jimmer.client.EnableImplicitApi;
import org.babyfish.jimmer.sql.EnableDtoGeneration;
import org.babyfish.jimmer.sql.meta.DatabaseNamingStrategy;
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.modulith.Modulithic;

@EnableImplicitApi
@Modulithic
@EnableDtoGeneration
@SpringBootApplication(scanBasePackages = {"com.zl.mjga"})
public class ApplicationService {

  public static void main(String[] args) {
    SpringApplication.run(ApplicationService.class, args);
  }

  @Bean
  public DatabaseNamingStrategy databaseNamingStrategy() {
    return DefaultDatabaseNamingStrategy.LOWER_CASE;
  }
}
