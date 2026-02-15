package com.caro.ingestion.internal.web

import jakarta.validation.constraints.NotBlank

data class UploadImageRequest(
    val memberId: Long,
    val deckId: Long,
    @field:NotBlank val imageData: String
)

data class UploadResponse(
    val jobId: String,
    val message: String
)
