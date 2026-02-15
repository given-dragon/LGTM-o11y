package com.caro.gamification.internal.event

import com.caro.gamification.internal.GamificationServiceImpl
import com.caro.review.event.CardReviewedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * CardReviewedEvent를 구독하여 경험치/스트릭/뱃지를 처리하는 리스너.
 * Best Effort 전략: 실패해도 원본 Review 트랜잭션에 영향 없음.
 */
private val log = KotlinLogging.logger {}

@Component
internal class ReviewEventListener(private val gamificationService: GamificationServiceImpl) {

    @Async("eventExecutor")
    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onCardReviewed(event: CardReviewedEvent) {
        log.info { "[Gamification] Processing: memberId=${event.memberId}, cardId=${event.cardId}, quality=${event.quality}" }
        try {
            val expGained = calculateExp(event.quality)
            gamificationService.addExpAndUpdateStreak(event.memberId, expGained, event.reviewedAt.toLocalDate())
        } catch (ex: Exception) {
            log.error(ex) { "[Gamification] Failed to process event: ${ex.message}" }
            // Best Effort: 로그만 남기고 예외를 삼킴
        }
    }

    private fun calculateExp(quality: Int): Long = when {
        quality >= 4 -> 15L
        quality >= 3 -> 10L
        else -> 5L
    }
}
