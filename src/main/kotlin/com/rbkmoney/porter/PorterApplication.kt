package com.rbkmoney.porter

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan

@ServletComponentScan
@ConfigurationPropertiesScan
@SpringBootApplication
class PorterApplication : SpringApplication()

fun main(args: Array<String>) {
    runApplication<PorterApplication>(*args)
}
