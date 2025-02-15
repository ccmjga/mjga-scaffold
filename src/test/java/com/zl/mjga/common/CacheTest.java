package com.zl.mjga.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.zl.mjga.common.application.CacheAppService;
import com.zl.mjga.common.cache.CacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {CacheConfig.class, CacheAppService.class})
public class CacheTest {

  @Autowired private CacheAppService cacheAppService;

  @Test
  void
      getVerifyCodeBy_upsertVerifyCodeBy_whenSetCacheValue_subsequentGetCacheShouldReturnUpdatedValue() {
    cacheAppService.upsertVerifyCodeBy("WsxOtE0d6Vc1glZ", "ej1x8T4XiluV8D216");
    String verifyCode = cacheAppService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    assertThat(verifyCode).isEqualTo("ej1x8T4XiluV8D216");
  }

  @Test
  void removeVerifyCodeBy_whenRemoveCacheValue_subsequentGetCacheShouldReturnNull() {
    cacheAppService.upsertVerifyCodeBy("WsxOtE0d6Vc1glZ", "ej1x8T4XiluV8D216");
    String verifyCode = cacheAppService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    cacheAppService.removeVerifyCodeBy("WsxOtE0d6Vc1glZ");
    String verifyCode2 = cacheAppService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    assertThat(verifyCode).isEqualTo("ej1x8T4XiluV8D216");
    assertThat(verifyCode2).isNull();
  }

  @Test
  void clearAllVerifyCode_whenCleanCache_subsequentGetCacheShouldReturnNewValue() {
    cacheAppService.upsertVerifyCodeBy("WsxOtE0d6Vc1glZ", "ej1x8T4XiluV8D216");
    cacheAppService.upsertVerifyCodeBy("hNYcK0MDjX4197", "Ll1v93jiXwHLji");
    String verifyCode1 = cacheAppService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    String verifyCode2 = cacheAppService.getVerifyCodeBy("hNYcK0MDjX4197");
    cacheAppService.clearAllVerifyCode();
    String verifyCode3 = cacheAppService.getVerifyCodeBy("WsxOtE0d6Vc1glZ");
    String verifyCode4 = cacheAppService.getVerifyCodeBy("hNYcK0MDjX4197");
    assertThat(verifyCode1).isEqualTo("ej1x8T4XiluV8D216");
    assertThat(verifyCode2).isEqualTo("Ll1v93jiXwHLji");
    assertThat(verifyCode3).isNull();
    assertThat(verifyCode4).isNull();
  }
}
