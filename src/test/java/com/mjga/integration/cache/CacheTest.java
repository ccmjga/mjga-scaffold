package com.mjga.integration.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.mjga.service.CacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CacheTest {

  @Autowired private CacheService cacheService;

  @Test
  void
      getVerifyCodeBy_upsertVerifyCodeBy_whenSetCacheValue_subsequentGetCacheShouldReturnUpdatedValue() {
    cacheService.upsertVerifyCodeBy("WsxOtE0d6Vc1glZ", "ej1x8T4XiluV8D216");
    String verifyCode = cacheService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    assertThat(verifyCode).isEqualTo("ej1x8T4XiluV8D216");
  }

  @Test
  void removeVerifyCodeBy_whenRemoveCacheValue_subsequentGetCacheShouldReturnNull() {
    cacheService.upsertVerifyCodeBy("WsxOtE0d6Vc1glZ", "ej1x8T4XiluV8D216");
    String verifyCode = cacheService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    cacheService.removeVerifyCodeBy("WsxOtE0d6Vc1glZ");
    String verifyCode2 = cacheService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    assertThat(verifyCode).isEqualTo("ej1x8T4XiluV8D216");
    assertThat(verifyCode2).isNull();
  }

  @Test
  void clearAllVerifyCode_whenCleanCache_subsequentGetCacheShouldReturnNewValue() {
    cacheService.upsertVerifyCodeBy("WsxOtE0d6Vc1glZ", "ej1x8T4XiluV8D216");
    cacheService.upsertVerifyCodeBy("hNYcK0MDjX4197", "Ll1v93jiXwHLji");
    String verifyCode1 = cacheService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    String verifyCode2 = cacheService.getVerifyCodeBy("hNYcK0MDjX4197");
    cacheService.clearAllVerifyCode();
    String verifyCode3 = cacheService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    String verifyCode4 = cacheService.getVerifyCodeBy("hNYcK0MDjX4197");
    assertThat(verifyCode1).isEqualTo("ej1x8T4XiluV8D216");
    assertThat(verifyCode2).isEqualTo("Ll1v93jiXwHLji");
    assertThat(verifyCode3).isNull();
    assertThat(verifyCode4).isNull();
  }
}
