package com.caro.observatory.internal.web

import com.caro.observatory.ErrorRequest
import com.caro.observatory.ExceptionRequest
import com.caro.observatory.ExceptionType
import com.caro.observatory.LatencyRequest
import com.caro.observatory.ObservatoryResult
import com.caro.shared.ApiResponse
import com.caro.shared.exception.InvalidRequestException
import com.caro.shared.exception.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

/**
 * Fault injection controller for testing observability under error conditions.
 *
 * 주요 테스트 대상:
 * - Tempo: 에러 span 시각화, 예외 이벤트 기록
 * - Loki: ERROR/WARN 로그, 스택트레이스
 * - Mimir: HTTP error rate 메트릭, HighHTTPErrorRate 알림 트리거
 * - Grafana: 에러 대시보드, 알림 연동
 */
@RestController
@RequestMapping("/api/observatory/fault")
internal class FaultController {

    @PostMapping("/latency")
    fun injectLatency(@Valid @RequestBody request: LatencyRequest): ApiResponse<ObservatoryResult> {
        val start = System.currentTimeMillis()
        val actualDelay = request.delayMs + (0..maxOf(1, request.jitterMs)).random()

        log.warn { "[Fault] Injecting latency: ${actualDelay}ms (base=${request.delayMs}ms, jitter=${request.jitterMs}ms)" }
        Thread.sleep(actualDelay)

        val elapsed = System.currentTimeMillis() - start
        return ApiResponse.ok(
            ObservatoryResult(
                operation = "latency-injection",
                durationMs = elapsed,
                details = mapOf("requestedDelayMs" to request.delayMs, "actualDelayMs" to actualDelay)
            )
        )
    }

    @PostMapping("/error")
    fun simulateError(@Valid @RequestBody request: ErrorRequest): ResponseEntity<ApiResponse<Nothing>> {
        log.error { "[Fault] Simulating HTTP ${request.statusCode}: ${request.message}" }
        return ResponseEntity
            .status(request.statusCode)
            .body(ApiResponse.error("SIMULATED_ERROR_${request.statusCode}", request.message))
    }

    @PostMapping("/exception")
    fun throwException(@Valid @RequestBody request: ExceptionRequest): ApiResponse<Nothing> {
        log.error { "[Fault] Throwing ${request.type}: ${request.message}" }

        when (request.type) {
            ExceptionType.RUNTIME -> throw RuntimeException(request.message)
            ExceptionType.NOT_FOUND -> throw NotFoundException("SimulatedEntity", request.message)
            ExceptionType.ILLEGAL_ARGUMENT -> throw InvalidRequestException(request.message)
            ExceptionType.ILLEGAL_STATE -> throw IllegalStateException(request.message)
            ExceptionType.OUT_OF_MEMORY -> throw OutOfMemoryError(request.message)
            ExceptionType.STACK_OVERFLOW -> triggerStackOverflow(0, request.message)
        }
    }

    private fun triggerStackOverflow(depth: Int, message: String): Nothing {
        triggerStackOverflow(depth + 1, message)
    }
}
