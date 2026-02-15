package com.caro.ingestion

data class IngestionJobDto(
    val jobId: String,
    val memberId: Long,
    val deckId: Long,
    val status: String,
    val message: String?
)
