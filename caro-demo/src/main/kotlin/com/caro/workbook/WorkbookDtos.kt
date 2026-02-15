package com.caro.workbook

import java.time.Instant

data class CardDto(
    val id: Long,
    val deckId: Long,
    val front: String,
    val back: String,
    val createdAt: Instant
)

data class DeckDto(
    val id: Long,
    val memberId: Long,
    val name: String,
    val description: String?,
    val createdAt: Instant
)
