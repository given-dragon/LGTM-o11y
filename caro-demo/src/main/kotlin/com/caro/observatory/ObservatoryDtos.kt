package com.caro.observatory

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

// -- Fault Injection --

data class LatencyRequest(
    @field:Min(0) @field:Max(30000)
    val delayMs: Long = 1000,
    val jitterMs: Long = 0
)

data class ErrorRequest(
    @field:Min(400) @field:Max(599)
    val statusCode: Int = 500,
    val message: String = "Simulated error"
)

data class ExceptionRequest(
    val type: ExceptionType = ExceptionType.RUNTIME,
    val message: String = "Simulated exception"
)

enum class ExceptionType {
    RUNTIME, NOT_FOUND, ILLEGAL_ARGUMENT, ILLEGAL_STATE, OUT_OF_MEMORY, STACK_OVERFLOW
}

// -- Trace Testing --

data class ChainRequest(
    @field:Positive
    val memberId: Long = 1,
    @field:Min(1) @field:Max(50)
    val cardCount: Int = 3,
    val includeLatencyMs: Long = 0
)

data class NestedSpanRequest(
    @field:Min(1) @field:Max(20)
    val depth: Int = 5,
    val delayPerSpanMs: Long = 50
)

data class ConcurrentSpanRequest(
    @field:Min(1) @field:Max(20)
    val concurrency: Int = 5,
    val workDurationMs: Long = 100
)

// -- Metric Testing --

data class CounterRequest(
    @field:NotBlank
    val name: String,
    val tags: Map<String, String> = emptyMap(),
    @field:Positive
    val increment: Double = 1.0
)

data class GaugeRequest(
    @field:NotBlank
    val name: String,
    val tags: Map<String, String> = emptyMap(),
    val value: Double
)

data class HistogramRequest(
    @field:NotBlank
    val name: String,
    val tags: Map<String, String> = emptyMap(),
    val value: Double
)

// -- Log Testing --

data class LogEmitRequest(
    val level: LogLevel = LogLevel.INFO,
    val message: String = "Observatory test log",
    val attributes: Map<String, String> = emptyMap()
)

enum class LogLevel { TRACE, DEBUG, INFO, WARN, ERROR }

data class LogBurstRequest(
    @field:Min(1) @field:Max(1000)
    val count: Int = 100,
    val level: LogLevel = LogLevel.INFO,
    val intervalMs: Long = 10
)

data class ErrorTraceRequest(
    val message: String = "Simulated error with stacktrace",
    @field:Min(1) @field:Max(10)
    val nestingDepth: Int = 3
)

// -- Scenario Testing --

data class StudySessionRequest(
    val nickname: String = "observatory-tester",
    val email: String = "observatory@test.com",
    val deckName: String = "Observatory Test Deck",
    @field:Min(1) @field:Max(50)
    val cardCount: Int = 5,
    @field:Min(0) @field:Max(5)
    val reviewQuality: Int = 4,
    val delayBetweenReviewsMs: Long = 100
)

data class MixedTrafficRequest(
    @field:Min(1) @field:Max(100)
    val totalRequests: Int = 20,
    @field:Min(0) @field:Max(100)
    val errorPercentage: Int = 20,
    val intervalMs: Long = 50
)

// -- Responses --

data class ObservatoryResult(
    val operation: String,
    val durationMs: Long,
    val details: Map<String, Any> = emptyMap()
)

data class TraceResult(
    val traceId: String,
    val spanCount: Int,
    val durationMs: Long,
    val details: Map<String, Any> = emptyMap()
)

data class ScenarioResult(
    val scenario: String,
    val steps: List<StepResult>,
    val totalDurationMs: Long
)

data class StepResult(
    val step: String,
    val success: Boolean,
    val durationMs: Long,
    val detail: String = ""
)
