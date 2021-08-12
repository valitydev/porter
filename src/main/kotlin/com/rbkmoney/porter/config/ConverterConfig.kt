package com.rbkmoney.porter.config

import com.rbkmoney.porter.converter.NotificatorConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.support.ConversionServiceFactoryBean

@Configuration
@ComponentScan(basePackages = ["com.rbkmoney.porter.converter"])
class ConverterConfig {

    @Bean
    @Primary
    fun conversionServiceFactoryBean(converters: Set<NotificatorConverter<*, *>>): ConversionServiceFactoryBean {
        val conversionServiceFactoryBean = ConversionServiceFactoryBean()
        conversionServiceFactoryBean.setConverters(converters)

        return conversionServiceFactoryBean
    }
}
