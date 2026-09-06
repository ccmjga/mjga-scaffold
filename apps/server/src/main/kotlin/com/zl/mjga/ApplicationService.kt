package com.zl.mjga

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.modulith.Modulithic
import org.springframework.scheduling.annotation.EnableScheduling

@Modulithic
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class ApplicationService

fun main(args: Array<String>) {
    runApplication<ApplicationService>(*args)
}
