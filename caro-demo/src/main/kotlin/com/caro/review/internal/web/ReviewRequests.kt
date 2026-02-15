package com.caro.review.internal.web

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive

data class RecordReviewRequest(
    val memberId: Long,
    val cardId: Long,
    val deckId: Long,
    @field:Min(0)
    @field:Max(5)
    val quality: Int,
    @field:Positive
    val reviewTimeMs: Long
)

data class InitializeCardRequest(
    val memberId: Long,
    val cardId: Long
)
