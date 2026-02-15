package com.caro.review.event

import java.time.LocalDateTime

/**
 * 카드 학습 완료 시 발행되는 도메인 이벤트.
 * 여러 모듈(Gamification, Analytics, Notification)이 구독하여 각자의 비즈니스 로직을 처리함.
 */
data class CardReviewedEvent(
    val reviewId: Long,
    val memberId: Long,
    val cardId: Long,
    val deckId: Long,
    val quality: Int,
    val reviewTimeMs: Long,
    val reviewedAt: LocalDateTime = LocalDateTime.now()
)