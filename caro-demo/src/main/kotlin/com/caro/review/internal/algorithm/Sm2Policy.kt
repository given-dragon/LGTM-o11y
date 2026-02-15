package com.caro.review.internal.algorithm

import java.time.LocalDate
import kotlin.math.max
import kotlin.math.roundToInt

object Sm2Policy {
    data class Sm2Result(val easeFactor: Double, val interval: Int, val repetitions: Int, val nextReviewDate: LocalDate)

    fun calculate(quality: Int, currentEaseFactor: Double, currentInterval: Int, currentRepetitions: Int): Sm2Result {
        require(quality in 0..5) { "Quality must be between 0 and 5" }
        return if (quality >= 3) {
            val newRepetitions = currentRepetitions + 1
            val newInterval = when (newRepetitions) { 1 -> 1; 2 -> 6; else -> (currentInterval * currentEaseFactor).roundToInt() }
            val newEaseFactor = max(1.3, currentEaseFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)))
            Sm2Result(newEaseFactor, newInterval, newRepetitions, LocalDate.now().plusDays(newInterval.toLong()))
        } else {
            Sm2Result(max(1.3, currentEaseFactor - 0.2), 1, 0, LocalDate.now().plusDays(1))
        }
    }
}
