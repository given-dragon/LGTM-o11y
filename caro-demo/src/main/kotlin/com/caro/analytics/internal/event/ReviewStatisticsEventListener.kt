package com.caro.analytics.internal.event

import com.caro.analytics.internal.AnalyticsServiceImpl
import com.caro.review.event.CardReviewedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * CardReviewedEvent를 구독하여 일간 학습 통계를 갱신하는 리스너.
 * Best Effort 전략: 실패해도 원본 Review 트랜잭션에 영향 없음.
 */
private val log = KotlinLogging.logger {}

@Component
internal class ReviewStatisticsEventListener(
    private val analyticsService: AnalyticsServiceImpl
) {

    @Async("eventExecutor")
    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onCardReviewed(event: CardReviewedEvent) {
        log.info { "[Analytics] Recording study: memberId=${event.memberId}, cardId=${event.cardId}, timeMs=${event.reviewTimeMs}" }
        try {
            analyticsService.recordStudy(
                memberId = event.memberId,
                reviewTimeMs = event.reviewTimeMs,
                studyDate = event.reviewedAt.toLocalDate()
            )
        } catch (ex: Exception) {
            log.error(ex) { "[Analytics] Failed to record study stats: ${ex.message}" }
            // Best Effort: 로그만 남기고 예외를 삼킴 (원본 트랜잭션 영향 없음)
        }
    }
}
