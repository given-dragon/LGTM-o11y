package com.caro.shared.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.VirtualThreadTaskExecutor
import org.springframework.core.task.support.ContextPropagatingTaskDecorator
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.lang.reflect.Method
import java.util.concurrent.Executor

/**
 * 비동기 이벤트 처리를 위한 Async 설정.
 * 이벤트 리스너들이 @Async로 실행될 때 사용할 스레드 풀을 정의함.
 * Best Effort Consistency를 위해 이벤트 처리는 별도 트랜잭션에서 비동기로 실행됨.
 */
private val log = KotlinLogging.logger {}

@Configuration
@EnableAsync
class AsyncConfig(
    private val contextPropagatingTaskDecorator: ContextPropagatingTaskDecorator
) : AsyncConfigurer {

    @Bean(name = ["eventExecutor"])
    fun eventExecutor(): Executor {
        return SimpleAsyncTaskExecutor().apply {
            setVirtualThreads(true)
            setTaskDecorator(contextPropagatingTaskDecorator)
        }
    }

    override fun getAsyncExecutor(): Executor = eventExecutor()

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncUncaughtExceptionHandler { ex: Throwable, method: Method, params: Array<out Any?> ->
            log.error(ex) { "Async event handling failed: method=${method.name}, params=${params.contentToString()}, error=${ex.message}" }
            // TODO: 실패한 이벤트를 Dead Letter Queue로 전송하거나 재시도 로직 추가 가능
        }
    }
}
