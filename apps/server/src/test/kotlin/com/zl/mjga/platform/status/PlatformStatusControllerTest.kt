package com.zl.mjga.platform.status

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class PlatformStatusControllerTest {
    @Test
    fun `reports the application identity through the business api`() {
        PlatformStatusController("orders", "1.2.3").status() shouldBe
            PlatformStatus("UP", "orders", "1.2.3")
    }
}
