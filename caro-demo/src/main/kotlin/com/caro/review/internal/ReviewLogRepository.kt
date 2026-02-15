package com.caro.review.internal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

internal interface ReviewLogRepository : JpaRepository<ReviewLog, Long> {
    fun findByMemberIdAndCardId(memberId: Long, cardId: Long): ReviewLog?

    @Query("SELECT r FROM ReviewLog r WHERE r.memberId = :memberId AND r.nextReviewDate <= :date")
    fun findDueReviews(memberId: Long, date: LocalDate): List<ReviewLog>

    @Query("SELECT r.cardId FROM ReviewLog r WHERE r.memberId = :memberId AND r.nextReviewDate <= :date")
    fun findDueCardIds(memberId: Long, date: LocalDate): List<Long>
}
