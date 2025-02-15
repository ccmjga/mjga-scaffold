package com.zl.mjga.common.application;

import com.zl.mjga.common.cache.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheAppService {
  @Cacheable(value = CacheConfig.VERIFY_CODE, key = "{#identify}", unless = "#result == null")
  public String getVerifyCodeBy(String identify) {
    return null;
  }

  @CachePut(value = CacheConfig.VERIFY_CODE, key = "{#identify}")
  public String upsertVerifyCodeBy(String identify, String value) {
    return value;
  }

  @CacheEvict(value = CacheConfig.VERIFY_CODE, key = "{#identify}")
  public void removeVerifyCodeBy(String identify) {}

  @CacheEvict(value = CacheConfig.VERIFY_CODE, allEntries = true)
  public void clearAllVerifyCode() {}
}
