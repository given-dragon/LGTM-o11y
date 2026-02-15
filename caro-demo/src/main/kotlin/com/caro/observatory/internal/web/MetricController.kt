package com.caro.observatory.internal.web

import com.caro.observatory.CounterRequest
import com.caro.observatory.GaugeRequest
import com.caro.observatory.HistogramRequest
import com.caro.observatory.ObservatoryResult
import com.caro.observatory.internal.CustomMetricsService
import com.caro.shared.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

/**
 * Custom metrics testing controller.
 *
 * 주요 테스트 대상:
 * - Mimir: PromQL 쿼리 (rate, histogram_quantile, increase)
 * - Grafana: 메트릭 대시보드 패널 구성, 알림 룰 설정
 * - Alloy: OTLP 메트릭 수집 파이프라인 검증
 *
 * 모든 메트릭은 "observatory." 접두사로 Mimir에 저장되어 다른 앱 메트릭과 구분됨.
 */
@RestController
@RequestMapping("/api/observatory/metrics")
internal class MetricController(private val metricsService: CustomMetricsService) {

    @PostMapping("/counter")
    fun incrementCounter(@Valid @RequestBody request: CounterRequest): ApiResponse<ObservatoryResult> {
        val start = System.currentTimeMillis()
        metricsService.incrementCounter(request.name, request.tags, request.increment)

        log.info { "[Metric] Counter incremented: ${request.name} += ${request.increment}" }
        return ApiResponse.ok(
            ObservatoryResult(
                operation = "counter-increment",
                durationMs = System.currentTimeMillis() - start,
                details = mapOf("name" to request.name, "increment" to request.increment, "tags" to request.tags)
            )
        )
    }

    @PostMapping("/gauge")
    fun setGauge(@Valid @RequestBody request: GaugeRequest): ApiResponse<ObservatoryResult> {
        val start = System.currentTimeMillis()
        metricsService.setGauge(request.name, request.tags, request.value)

        log.info { "[Metric] Gauge set: ${request.name} = ${request.value}" }
        return ApiResponse.ok(
            ObservatoryResult(
                operation = "gauge-set",
                durationMs = System.currentTimeMillis() - start,
                details = mapOf("name" to request.name, "value" to request.value, "tags" to request.tags)
            )
        )
    }

    @PostMapping("/histogram")
    fun recordHistogram(@Valid @RequestBody request: HistogramRequest): ApiResponse<ObservatoryResult> {
        val start = System.currentTimeMillis()
        metricsService.recordHistogram(request.name, request.tags, request.value)

        log.info { "[Metric] Histogram recorded: ${request.name} = ${request.value}" }
        return ApiResponse.ok(
            ObservatoryResult(
                operation = "histogram-record",
                durationMs = System.currentTimeMillis() - start,
                details = mapOf("name" to request.name, "value" to request.value, "tags" to request.tags)
            )
        )
    }

    @GetMapping("/registered")
    fun getRegisteredMetrics(): ApiResponse<List<String>> {
        val metrics = metricsService.getRegisteredMetricNames()
        log.info { "[Metric] Listed ${metrics.size} observatory metrics" }
        return ApiResponse.ok(metrics)
    }
}
