package com.zl.mjga.platform.events

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.time.Duration

private const val MAX_BATCH_SIZE = 1000L
private const val MAX_CONCURRENCY = 64L
private const val MAX_ATTEMPTS = 100L

@Validated
@ConfigurationProperties("mjga.events")
data class EventPublicationProperties(
    val managementEnabled: Boolean,
    val managementUsername: String?,
    val managementPassword: String?,
    val initialDelay: Duration,
    val scanInterval: Duration,
    @field:Min(1) @field:Max(MAX_BATCH_SIZE) val batchSize: Int,
    @field:Min(1) @field:Max(MAX_CONCURRENCY) val concurrency: Int,
    @field:Min(1) @field:Max(MAX_ATTEMPTS) val maxAttempts: Int,
    val minimumAge: Duration,
    val completedRetention: Duration,
    val auditRetention: Duration,
    @field:Min(1) @field:Max(MAX_BATCH_SIZE) val managementBatchLimit: Int,
) {
    init {
        require(!managementEnabled || (!managementUsername.isNullOrBlank() && !managementPassword.isNullOrBlank())) {
            "mjga.events management credentials are required when management is enabled"
        }
        requirePositive(initialDelay, "initial-delay")
        requirePositive(scanInterval, "scan-interval")
        requirePositive(minimumAge, "minimum-age")
        requirePositive(completedRetention, "completed-retention")
        requirePositive(auditRetention, "audit-retention")
    }

    private fun requirePositive(
        value: Duration,
        name: String,
    ) {
        require(!value.isZero && !value.isNegative) { "mjga.events.$name must be positive" }
    }
}
