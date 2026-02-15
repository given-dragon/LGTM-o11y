package com.caro.ingestion.internal.web

import com.caro.ingestion.IngestionJobDto
import com.caro.ingestion.IngestionService
import com.caro.shared.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ingestion")
class IngestionController(private val ingestionService: IngestionService) {

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun uploadImage(@Valid @RequestBody request: UploadImageRequest): ApiResponse<UploadResponse> {
        val jobId = ingestionService.submitIngestion(request.memberId, request.deckId, request.imageData)
        return ApiResponse.ok(UploadResponse(jobId, "Processing started"))
    }

    @GetMapping("/status/{jobId}")
    fun getJobStatus(@PathVariable jobId: String): ApiResponse<IngestionJobDto> {
        val job = ingestionService.getJobStatus(jobId) ?: return ApiResponse.error("NOT_FOUND", "Job not found: $jobId")
        return ApiResponse.ok(job)
    }
}


