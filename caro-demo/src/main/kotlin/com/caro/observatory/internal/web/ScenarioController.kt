package com.caro.observatory.internal.web

import com.caro.member.MemberService
import com.caro.observatory.MixedTrafficRequest
import com.caro.observatory.ObservatoryResult
import com.caro.observatory.ScenarioResult
import com.caro.observatory.StepResult
import com.caro.observatory.StudySessionRequest
import com.caro.review.ReviewService
import com.caro.shared.ApiResponse
import com.caro.workbook.CreateCardCommand
import com.caro.workbook.WorkbookService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

/**
 * End-to-end scenario controller.
 * 실제 모듈 간 상호작용을 트리거하여 전체 관측 파이프라인을 테스트.
 *
 * 주요 테스트 대상:
 * - Tempo: 모듈 간 분산 트레이싱 (member→workbook→review→gamification/analytics/notification)
 * - Loki: 모듈별 로그 상관관계, trace_id 기반 로그 연동
 * - Mimir: 비즈니스 메트릭 (review count, study time), HTTP 메트릭
 * - Grafana: Service Map, 로그-트레이스-메트릭 3대 기둥 연동 확인
 *
 * study-session 시나리오 실행 시 발생하는 관측 데이터:
 * 1. HTTP request trace span (ScenarioController)
 * 2. MemberService.createMember → DB write span
 * 3. WorkbookService.createDeck → DB write span
 * 4. WorkbookService.createCard x N → DB write spans
 * 5. ReviewService.initializeCardForReview x N → DB write spans
 * 6. ReviewService.recordReview x N → DB write + event publish
 *    ├─ CardReviewedEvent → GamificationService (async, separate trace segment)
 *    ├─ CardReviewedEvent → AnalyticsService (async, separate trace segment)
 *    └─ CardReviewedEvent → NotificationService (async, separate trace segment)
 */
@RestController
@RequestMapping("/api/observatory/scenarios")
internal class ScenarioController(
    private val memberService: MemberService,
    private val workbookService: WorkbookService,
    private val reviewService: ReviewService,
    openTelemetry: OpenTelemetry
) {
    private val tracer: Tracer = openTelemetry.getTracer("observatory.scenario")

    @PostMapping("/study-session")
    fun fullStudySession(@Valid @RequestBody request: StudySessionRequest): ApiResponse<ScenarioResult> {
        val totalStart = System.currentTimeMillis()
        val steps = mutableListOf<StepResult>()

        log.info { "[Scenario] Starting full study session: ${request.nickname}, ${request.cardCount} cards" }

        // Step 1: Create or retrieve member
        val memberId = executeStep(steps, "create-member") {
            val existing = memberService.getMemberByEmail(request.email)
            if (existing != null) {
                log.info { "[Scenario] Member already exists: id=${existing.id}" }
                existing.id
            } else {
                val member = memberService.createMember(request.email, request.nickname)
                log.info { "[Scenario] Member created: id=${member.id}" }
                member.id
            }
        }

        // Step 2: Create deck
        val deckId = executeStep(steps, "create-deck") {
            val deck = workbookService.createDeck(memberId, request.deckName, "Observatory test deck")
            log.info { "[Scenario] Deck created: id=${deck.id}" }
            deck.id
        }

        // Step 3: Create cards
        val cardIds = executeStep(steps, "create-cards") {
            val cards = (1..request.cardCount).map { i ->
                workbookService.createCard(
                    CreateCardCommand(
                        deckId = deckId,
                        front = "Observatory Q$i: What is observability pillar #$i?",
                        back = "Observatory A$i: ${observabilityAnswer(i)}",
                        memberId = memberId
                    )
                )
            }
            log.info { "[Scenario] ${cards.size} cards created" }
            cards.map { it.id }
        }

        // Step 4: Initialize cards for review
        executeStep(steps, "initialize-reviews") {
            cardIds.forEach { cardId ->
                reviewService.initializeCardForReview(memberId, cardId)
            }
            log.info { "[Scenario] ${cardIds.size} cards initialized for review" }
        }

        // Step 5: Record reviews (triggers CardReviewedEvent fan-out)
        executeStep(steps, "record-reviews") {
            cardIds.forEachIndexed { index, cardId ->
                val span = tracer.spanBuilder("review-card-${index + 1}")
                    .setSpanKind(SpanKind.INTERNAL)
                    .setAttribute("card.id", cardId)
                    .setAttribute("card.index", (index + 1).toLong())
                    .setAttribute("review.quality", request.reviewQuality.toLong())
                    .startSpan()

                span.makeCurrent().use {
                    val reviewTimeMs = (500..3000).random().toLong()
                    reviewService.recordReview(
                        memberId = memberId,
                        cardId = cardId,
                        deckId = deckId,
                        quality = request.reviewQuality,
                        reviewTimeMs = reviewTimeMs
                    )
                    log.info { "[Scenario] Review ${index + 1}/${cardIds.size}: cardId=$cardId, quality=${request.reviewQuality}, timeMs=$reviewTimeMs" }
                }
                span.end()

                if (request.delayBetweenReviewsMs > 0) {
                    Thread.sleep(request.delayBetweenReviewsMs)
                }
            }
        }

        val totalDuration = System.currentTimeMillis() - totalStart
        log.info { "[Scenario] Study session completed in ${totalDuration}ms: ${steps.size} steps, ${steps.count { it.success }} succeeded" }

        return ApiResponse.ok(
            ScenarioResult(
                scenario = "study-session",
                steps = steps,
                totalDurationMs = totalDuration
            )
        )
    }

    @PostMapping("/mixed-traffic")
    fun mixedTraffic(@Valid @RequestBody request: MixedTrafficRequest): ApiResponse<ObservatoryResult> {
        val start = System.currentTimeMillis()
        var successCount = 0
        var errorCount = 0

        log.info { "[Scenario] Starting mixed traffic: total=${request.totalRequests}, errorRate=${request.errorPercentage}%" }

        repeat(request.totalRequests) { index ->
            val shouldFail = (index * 100 / request.totalRequests) < request.errorPercentage

            val span = tracer.spanBuilder("mixed-traffic-${index + 1}")
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("request.index", (index + 1).toLong())
                .setAttribute("request.should_fail", shouldFail)
                .startSpan()

            span.makeCurrent().use {
                if (shouldFail) {
                    log.error { "[Scenario] Mixed traffic request ${index + 1}: simulated failure" }
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "Simulated failure")
                    errorCount++
                } else {
                    log.info { "[Scenario] Mixed traffic request ${index + 1}: success" }
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.OK)
                    successCount++
                }
            }
            span.end()

            if (request.intervalMs > 0) Thread.sleep(request.intervalMs)
        }

        val elapsed = System.currentTimeMillis() - start
        log.info { "[Scenario] Mixed traffic completed: success=$successCount, error=$errorCount, duration=${elapsed}ms" }

        return ApiResponse.ok(
            ObservatoryResult(
                operation = "mixed-traffic",
                durationMs = elapsed,
                details = mapOf(
                    "totalRequests" to request.totalRequests,
                    "successCount" to successCount,
                    "errorCount" to errorCount,
                    "actualErrorRate" to (errorCount * 100.0 / request.totalRequests)
                )
            )
        )
    }

    private fun <T> executeStep(steps: MutableList<StepResult>, stepName: String, action: () -> T): T {
        val stepStart = System.currentTimeMillis()
        return try {
            val result = action()
            steps.add(StepResult(step = stepName, success = true, durationMs = System.currentTimeMillis() - stepStart))
            result
        } catch (ex: Exception) {
            log.error(ex) { "[Scenario] Step '$stepName' failed: ${ex.message}" }
            steps.add(
                StepResult(
                    step = stepName,
                    success = false,
                    durationMs = System.currentTimeMillis() - stepStart,
                    detail = ex.message ?: "Unknown error"
                )
            )
            throw ex
        }
    }

    private fun observabilityAnswer(index: Int): String = when (index % 5) {
        1 -> "Metrics - quantitative measurements of system behavior over time"
        2 -> "Logs - discrete events with structured context information"
        3 -> "Traces - distributed request flow across service boundaries"
        4 -> "Alerting - automated detection and notification of anomalies"
        0 -> "Dashboards - visual representation of system health and performance"
        else -> "Observability enables understanding complex systems through external outputs"
    }
}
