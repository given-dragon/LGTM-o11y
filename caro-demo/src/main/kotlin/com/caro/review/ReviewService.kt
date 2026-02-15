package com.caro.review

/**
 * Review 모듈의 공개 API.
 * 카드 학습 기록 및 스케줄링 담당.
 */
interface ReviewService {
    fun getTodayReviewCardIds(memberId: Long): List<Long>

    /**
     * 카드 학습 결과를 기록하고 CardReviewedEvent를 발행함.
     * @param memberId 학습한 회원 ID
     * @param cardId 학습한 카드 ID
     * @param deckId 카드가 속한 덱 ID
     * @param quality 학습 점수 (0~5, SM-2 알고리즘 기준)
     * @param reviewTimeMs 학습에 소요된 시간 (밀리초)
     */
    fun recordReview(memberId: Long, cardId: Long, deckId: Long, quality: Int, reviewTimeMs: Long)

    fun getTodayReviews(memberId: Long): List<ReviewLogDto>
    fun initializeCardForReview(memberId: Long, cardId: Long): ReviewLogDto
}


