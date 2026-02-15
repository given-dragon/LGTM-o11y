package com.caro.observatory.internal

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

private val log = KotlinLogging.logger {}

/**
 * Custom metrics helper service.
 * Micrometer MeterRegistry를 래핑하여 동적으로 counter/gauge/histogram을 생성하고 관리.
 *
 * Micrometer는 동일 이름+태그 조합의 meter를 중복 등록하면 기존 것을 반환하므로,
 * 여기서는 ConcurrentHashMap으로 AtomicReference<Double>를 관리하여 gauge 값을 안전하게 업데이트.
 */
@Service
internal class CustomMetricsService(private val meterRegistry: MeterRegistry) {

    private val gaugeValues = ConcurrentHashMap<String, AtomicReference<Double>>()

    fun incrementCounter(name: String, tags: Map<String, String>, amount: Double) {
        val prefixedName = "observatory.$name"
        val micrometerTags = Tags.of(tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) })

        Counter.builder(prefixedName)
            .tags(micrometerTags)
            .description("Observatory custom counter: $name")
            .register(meterRegistry)
            .increment(amount)

        log.info { "Counter incremented: $prefixedName by $amount, tags=$tags" }
    }

    fun setGauge(name: String, tags: Map<String, String>, value: Double) {
        val prefixedName = "observatory.$name"
        val tagString = tags.entries.sortedBy { it.key }.joinToString(",") { "${it.key}=${it.value}" }
        val key = "$prefixedName:$tagString"

        val ref = gaugeValues.computeIfAbsent(key) { atomicRef ->
            val micrometerTags = Tags.of(tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) })
            val newRef = AtomicReference(value)
            meterRegistry.gauge(prefixedName, micrometerTags, newRef) { it.get() }
            newRef
        }
        ref.set(value)

        log.info { "Gauge set: $prefixedName = $value, tags=$tags" }
    }

    fun recordHistogram(name: String, tags: Map<String, String>, value: Double) {
        val prefixedName = "observatory.$name"
        val micrometerTags = Tags.of(tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) })

        DistributionSummary.builder(prefixedName)
            .tags(micrometerTags)
            .description("Observatory custom histogram: $name")
            .publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99)
            .register(meterRegistry)
            .record(value)

        log.info { "Histogram recorded: $prefixedName = $value, tags=$tags" }
    }

    fun getRegisteredMetricNames(): List<String> =
        meterRegistry.meters
            .map { it.id.name }
            .filter { it.startsWith("observatory.") }
            .distinct()
            .sorted()
}
