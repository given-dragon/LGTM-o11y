package com.caro.ingestion

import java.time.Instant

data class IngestionCompletedEvent(
    val jobId: String,
    val memberId: Long,
    val deckId: Long,
    val extractedCards: List<ExtractedCard>,
    val completedAt: Instant = Instant.now()
)

data class ExtractedCard(val front: String, val back: String)
