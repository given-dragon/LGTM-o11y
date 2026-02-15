package com.caro.review.exception

sealed class ReviewException(message: String) : RuntimeException(message) {
    class CardNotFoundException(cardId: Long) : ReviewException("Card not found: $cardId")
    class InvalidReviewDataException(message: String) : ReviewException(message)
}
