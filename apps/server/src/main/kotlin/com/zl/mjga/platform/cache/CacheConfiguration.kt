package com.zl.mjga.platform.cache

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration(proxyBeanMethods = false)
@EnableCaching
class CacheConfiguration {
    @Bean
    fun cacheManager(): CacheManager =
        CaffeineCacheManager().apply {
            setCaffeine(
                Caffeine
                    .newBuilder()
                    .maximumSize(MAXIMUM_CACHE_ENTRIES)
                    .expireAfterAccess(Duration.ofMinutes(EXPIRE_AFTER_ACCESS_MINUTES)),
            )
        }

    private companion object {
        const val MAXIMUM_CACHE_ENTRIES = 10_000L
        const val EXPIRE_AFTER_ACCESS_MINUTES = 30L
    }
}
