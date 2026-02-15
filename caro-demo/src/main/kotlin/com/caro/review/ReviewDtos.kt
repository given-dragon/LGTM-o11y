package com.caro.review

import java.time.LocalDate

data class ReviewLogDto(
    val id: Long,
    val memberId: Long,
    val cardId: Long,
    val easeFactor: Double,
    val interval: Int,
    val repetitions: Int,
    val nextReviewDate: LocalDate
)
