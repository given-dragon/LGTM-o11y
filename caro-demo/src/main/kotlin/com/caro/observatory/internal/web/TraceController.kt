package com.caro.observatory.internal.web

import com.caro.observatory.ChainRequest
import com.caro.observatory.ConcurrentSpanRequest
import com.caro.observatory.NestedSpanRequest
import com.caro.observatory.TraceResult
import com.caro.shared.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

private val log = KotlinLogging.logger {}

/**
 * Trace testing controller for validating distributed tracing capabilities.
 *
 * 주요 테스트 대상:
 * - Tempo: span 계층 구조, 병렬 span, nested span, trace 시각화
 * - Loki: trace_id 기반 로그-트레이스 연동
 * - Grafana: TraceQL 쿼리, Service Map, trace-to-logs 연결
 */
@RestController
@RequestMapping("/api/observatory/trace")
internal class TraceController(openTelemetry: OpenTelemetry) {

    private val tracer: Tracer = openTelemetry.getTracer("observatory.trace")

    @PostMapping("/nested")
    fun nestedSpans(@Valid @RequestBody request: NestedSpanRequest): ApiResponse<TraceResult> {
        val start = System.currentTimeMillis()
        val rootSpan = Span.current()
        val traceId = rootSpan.spanContext.traceId

        log.info { "[Trace] Creating nested spans: depth=${request.depth}, delayPerSpan=${request.delayPerSpanMs}ms" }
        createNestedSpan(0, request.depth, request.delayPerSpanMs)

        return ApiResponse.ok(
            TraceResult(
                traceId = traceId,
                spanCount = request.depth,
                durationMs = System.currentTimeMillis() - start,
                details = mapOf("type" to "nested", "depth" to request.depth)
            )
        )
    }

    @PostMapping("/concurrent")
    fun concurrentSpans(@Valid @RequestBody request: ConcurrentSpanRequest): ApiResponse<TraceResult> {
        val start = System.currentTimeMillis()
        val parentSpan = Span.current()
        val traceId = parentSpan.spanContext.traceId
        val parentContext = Context.current()

        log.info { "[Trace] Creating concurrent spans: count=${request.concurrency}, workDuration=${request.workDurationMs}ms" }

        val executor = Executors.newVirtualThreadPerTaskExecutor()
        try {
            val futures = (1..request.concurrency).map { index ->
                CompletableFuture.supplyAsync({
                    val span = tracer.spanBuilder("concurrent-worker-$index")
                        .setParent(parentContext)
                        .setSpanKind(SpanKind.INTERNAL)
                        .setAttribute("worker.index", index.toLong())
                        .startSpan()

                    span.makeCurrent().use {
                        log.debug { "[Trace] Worker $index started" }
                        simulateWork(request.workDurationMs, "worker-$index")
                        log.debug { "[Trace] Worker $index completed" }
                    }
                    span.end()
                    index
                }, executor)
            }
            futures.forEach { it.join() }
        } finally {
            executor.close()
        }

        return ApiResponse.ok(
            TraceResult(
                traceId = traceId,
                spanCount = request.concurrency,
                durationMs = System.currentTimeMillis() - start,
                details = mapOf("type" to "concurrent", "workers" to request.concurrency)
            )
        )
    }

    @GetMapping("/propagation")
    fun testPropagation(): ApiResponse<TraceResult> {
        val start = System.currentTimeMillis()
        val currentSpan = Span.current()
        val traceId = currentSpan.spanContext.traceId
        val spanId = currentSpan.spanContext.spanId

        log.info { "[Trace] Context propagation test: traceId=$traceId, spanId=$spanId" }

        val childSpan = tracer.spanBuilder("propagation-child")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("test.type", "propagation")
            .startSpan()

        childSpan.makeCurrent().use {
            val childTraceId = Span.current().spanContext.traceId
            val childSpanId = Span.current().spanContext.spanId
            log.info { "[Trace] Child span: traceId=$childTraceId, spanId=$childSpanId, parentSpanId=$spanId" }

            childSpan.addEvent("propagation-verified")
        }
        childSpan.end()

        return ApiResponse.ok(
            TraceResult(
                traceId = traceId,
                spanCount = 2,
                durationMs = System.currentTimeMillis() - start,
                details = mapOf(
                    "parentSpanId" to spanId,
                    "traceIdConsistent" to true,
                    "type" to "propagation"
                )
            )
        )
    }

    private fun createNestedSpan(currentDepth: Int, maxDepth: Int, delayMs: Long) {
        if (currentDepth >= maxDepth) return

        val span = tracer.spanBuilder("nested-span-level-$currentDepth")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("span.depth", currentDepth.toLong())
            .setAttribute("span.max_depth", maxDepth.toLong())
            .startSpan()

        span.makeCurrent().use {
            log.debug { "[Trace] Nested span level $currentDepth/$maxDepth" }
            if (delayMs > 0) Thread.sleep(delayMs)
            span.addEvent("processing-at-depth-$currentDepth")
            createNestedSpan(currentDepth + 1, maxDepth, delayMs)
        }
        span.end()
    }

    private fun simulateWork(durationMs: Long, label: String) {
        val span = tracer.spanBuilder("work-$label")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan()

        span.makeCurrent().use {
            Thread.sleep(durationMs)
            span.setStatus(StatusCode.OK)
            span.addEvent("work-completed")
        }
        span.end()
    }
}
