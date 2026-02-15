package com.caro.ingestion.exception

sealed class IngestionException(message: String) : RuntimeException(message) {
    class JobNotFoundException(jobId: String) : IngestionException("Ingestion job not found: $jobId")
    class IngestionFailedException(message: String) : IngestionException(message)
}
