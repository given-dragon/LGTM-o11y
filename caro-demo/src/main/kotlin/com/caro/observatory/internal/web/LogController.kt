package com.caro.observatory.internal.web

import com.caro.observatory.ErrorTraceRequest
import com.caro.observatory.LogBurstRequest
import com.caro.observatory.LogEmitRequest
import com.caro.observatory.LogLevel
import com.caro.observatory.ObservatoryResult
import com.caro.shared.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.slf4j.MDC
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

/**
 * Log testing controller for validating log aggregation and correlation.
 *
 * 주요 테스트 대상:
 * - Loki: LogQL 쿼리, 라벨 필터링, 로그 레벨별 필터
 * - Tempo: trace_id 기반 로그-트레이스 연동 (Derived Fields)
 * - Grafana: 로그 대시보드, Log-to-Trace 링크, Explore 뷰
 *
 * 모든 로그는 OTel Logback Appender를 통해 Alloy → Loki로 전송됨.
 * trace_id/span_id가 자동으로 MDC에 포함되어 로그-트레이스 상관관계 확보.
 */
@RestController
@RequestMapping("/api/observatory/logs")
internal class LogController {

    @PostMapping("/emit")
    fun emitLog(@Valid @RequestBody request: LogEmitRequest): ApiResponse<ObservatoryResult> {
        val start = System.currentTimeMillis()

        // MDC에 커스텀 속성 추가 (Loki에서 structured metadata로 조회 가능)
        request.attributes.forEach { (key, value) -> MDC.put("observatory.$key", value) }

        try {
            emitAtLevel(request.level, request.message)
        } finally {
            request.attributes.keys.forEach { MDC.remove("observatory.$it") }
        }

        return ApiResponse.ok(
            ObservatoryResult(
                operation = "log-emit",
                durationMs = System.currentTimeMillis() - start,
                details = mapOf(
                    "level" to request.level.name,
                    "attributeCount" to request.attributes.size
                )
            )
        )
    }

    @PostMapping("/burst")
    fun logBurst(@Valid @RequestBody request: LogBurstRequest): ApiResponse<ObservatoryResult> {
        val start = System.currentTimeMillis()

        log.info { "[Log] Starting burst: count=${request.count}, level=${request.level}, interval=${request.intervalMs}ms" }

        repeat(request.count) { index ->
            emitAtLevel(request.level, "[Log] Burst message ${index + 1}/${request.count}")
            if (request.intervalMs > 0) Thread.sleep(request.intervalMs)
        }

        val elapsed = System.currentTimeMillis() - start
        log.info { "[Log] Burst completed: ${request.count} messages in ${elapsed}ms" }

        return ApiResponse.ok(
            ObservatoryResult(
                operation = "log-burst",
                durationMs = elapsed,
                details = mapOf(
                    "count" to request.count,
                    "level" to request.level.name,
                    "messagesPerSecond" to if (elapsed > 0) (request.count * 1000.0 / elapsed) else 0.0
                )
            )
        )
    }

    @PostMapping("/error-trace")
    fun errorWithStacktrace(@Valid @RequestBody request: ErrorTraceRequest): ApiResponse<ObservatoryResult> {
        val start = System.currentTimeMillis()

        log.info { "[Log] Generating error with nested exception: depth=${request.nestingDepth}" }

        val exception = buildNestedException(request.nestingDepth, request.message)
        log.error(exception) { "[Log] Simulated error: ${request.message}" }

        return ApiResponse.ok(
            ObservatoryResult(
                operation = "error-trace",
                durationMs = System.currentTimeMillis() - start,
                details = mapOf(
                    "nestingDepth" to request.nestingDepth,
                    "rootCauseMessage" to (findRootCause(exception).message ?: "unknown")
                )
            )
        )
    }

    private fun emitAtLevel(level: LogLevel, message: String) {
        when (level) {
            LogLevel.TRACE -> log.trace { message }
            LogLevel.DEBUG -> log.debug { message }
            LogLevel.INFO -> log.info { message }
            LogLevel.WARN -> log.warn { message }
            LogLevel.ERROR -> log.error { message }
        }
    }

    private fun buildNestedException(depth: Int, message: String): Exception {
        var current: Exception = RuntimeException("Root cause: $message")
        repeat(depth - 1) { level ->
            current = RuntimeException("Wrapped at level ${level + 1}", current)
        }
        return current
    }

    private fun findRootCause(throwable: Throwable): Throwable =
        throwable.cause?.let { findRootCause(it) } ?: throwable
}
