package com.zl.mjga.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
public class CacheConfig {

  public static final String VERIFY_CODE = "verifyCode";

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(List.of(verifyCodeCache()));
    return cacheManager;
  }

  private CaffeineCache verifyCodeCache() {
    return new CaffeineCache(
        VERIFY_CODE,
        Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(60, TimeUnit.SECONDS).build());
  }
}
