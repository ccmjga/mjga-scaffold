package com.zl.mjga.platform.events

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Duration

@Tag("unit")
class EventPublicationPropertiesTest {
    @Test
    fun `defaults remain bounded and auditable`() {
        val properties = properties()
        assertThat(properties.maxAttempts).isEqualTo(10)
        assertThat(properties.completedRetention).isEqualTo(Duration.ofDays(30))
        assertThat(properties.auditRetention).isEqualTo(Duration.ofDays(365))
    }

    @Test
    fun `management cannot be enabled without independent credentials`() {
        assertThatThrownBy { properties(managementEnabled = true) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("management credentials are required")
    }

    private fun properties(managementEnabled: Boolean = false) =
        EventPublicationProperties(
            managementEnabled,
            null,
            null,
            Duration.ofSeconds(30),
            Duration.ofSeconds(30),
            100,
            4,
            10,
            Duration.ofSeconds(30),
            Duration.ofDays(30),
            Duration.ofDays(365),
            100,
        )
}
