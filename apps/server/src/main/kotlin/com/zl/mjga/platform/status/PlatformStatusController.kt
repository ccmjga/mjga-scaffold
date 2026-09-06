package com.zl.mjga.platform.status

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PlatformStatusController(
    @param:Value("\${spring.application.name}") private val application: String,
    @param:Value("\${info.app.version:1.0.0}") private val version: String,
) {
    @GetMapping("/api/v1/platform/status")
    fun status() = PlatformStatus("UP", application, version)
}

data class PlatformStatus(
    val status: String,
    val application: String,
    val version: String,
)
