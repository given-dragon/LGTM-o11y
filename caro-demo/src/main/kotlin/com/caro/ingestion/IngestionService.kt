package com.caro.ingestion

interface IngestionService {
    fun submitIngestion(memberId: Long, deckId: Long, imageData: String): String
    fun getJobStatus(jobId: String): IngestionJobDto?
}

