package com.caro.review.internal

import com.caro.review.event.CardReviewedEvent
import com.caro.review.ReviewLogDto
import com.caro.review.ReviewService
import com.caro.review.exception.ReviewException
import com.caro.review.internal.algorithm.Sm2Policy
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

@Service
internal class ReviewServiceImpl(
    private val reviewLogRepository: ReviewLogRepository,
    private val eventPublisher: ApplicationEventPublisher
) : ReviewService {

    @Transactional(readOnly = true)
    override fun getTodayReviewCardIds(memberId: Long): List<Long> = reviewLogRepository.findDueCardIds(memberId, LocalDate.now())

    @Transactional
    override fun recordReview(memberId: Long, cardId: Long, deckId: Long, quality: Int, reviewTimeMs: Long) {
        if (quality !in 0..5) throw ReviewException.InvalidReviewDataException("Quality must be between 0 and 5")

        val reviewLog = reviewLogRepository.findByMemberIdAndCardId(memberId, cardId)
            ?: ReviewLog(memberId = memberId, cardId = cardId)

        val result = Sm2Policy.calculate(quality, reviewLog.easeFactor, reviewLog.reviewInterval, reviewLog.repetitions)

        reviewLog.apply {
            this.quality = quality
            easeFactor = result.easeFactor
            reviewInterval = result.interval
            repetitions = result.repetitions
            nextReviewDate = result.nextReviewDate
            updatedAt = Instant.now()
        }

        val savedLog = reviewLogRepository.save(reviewLog)
        log.info { "Review recorded: memberId=$memberId, cardId=$cardId, quality=$quality, nextReview=${result.nextReviewDate}" }

        // 이벤트 발행: 여러 모듈(Gamification, Analytics, Notification)이 구독함
        eventPublisher.publishEvent(
            CardReviewedEvent(
                reviewId = savedLog.id,
                memberId = memberId,
                cardId = cardId,
                deckId = deckId,
                quality = quality,
                reviewTimeMs = reviewTimeMs,
                reviewedAt = LocalDateTime.now()
            )
        )
    }

    @Transactional(readOnly = true)
    override fun getTodayReviews(memberId: Long): List<ReviewLogDto> = reviewLogRepository.findDueReviews(memberId, LocalDate.now()).map { it.toDto() }

    @Transactional
    override fun initializeCardForReview(memberId: Long, cardId: Long): ReviewLogDto {
        val existing = reviewLogRepository.findByMemberIdAndCardId(memberId, cardId)
        return (existing ?: reviewLogRepository.save(ReviewLog(memberId = memberId, cardId = cardId))).toDto()
    }

    private fun ReviewLog.toDto() = ReviewLogDto(id, memberId, cardId, easeFactor, reviewInterval, repetitions, nextReviewDate)
}
