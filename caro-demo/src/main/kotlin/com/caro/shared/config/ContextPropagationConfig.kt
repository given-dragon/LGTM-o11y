package com.caro.shared.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.support.ContextPropagatingTaskDecorator

@Configuration(proxyBeanMethods = false)
class ContextPropagationConfig {
    @Bean
    fun contextPropagationDecorator(): ContextPropagatingTaskDecorator {
        return ContextPropagatingTaskDecorator()
    }
}