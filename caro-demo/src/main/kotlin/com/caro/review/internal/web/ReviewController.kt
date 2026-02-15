package com.caro.review.internal.web

import com.caro.review.ReviewLogDto
import com.caro.review.ReviewService
import com.caro.shared.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/review")
class ReviewController(
    private val reviewService: ReviewService
) {
    @GetMapping("/today")
    fun getTodayReviewCardIds(@RequestParam memberId: Long): ApiResponse<List<Long>> =
        ApiResponse.ok(reviewService.getTodayReviewCardIds(memberId))

    @GetMapping("/today/details")
    fun getTodayReviews(@RequestParam memberId: Long): ApiResponse<List<ReviewLogDto>> =
        ApiResponse.ok(reviewService.getTodayReviews(memberId))

    @PostMapping
    fun recordReview(@Valid @RequestBody request: RecordReviewRequest): ApiResponse<String> {
        reviewService.recordReview(
            memberId = request.memberId,
            cardId = request.cardId,
            deckId = request.deckId,
            quality = request.quality,
            reviewTimeMs = request.reviewTimeMs
        )
        return ApiResponse.ok("Review recorded successfully")
    }

    @PostMapping("/initialize")
    fun initializeCard(@RequestBody request: InitializeCardRequest): ApiResponse<ReviewLogDto> =
        ApiResponse.ok(reviewService.initializeCardForReview(request.memberId, request.cardId))
}


