package com.caro.review.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "review_logs")
internal class ReviewLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false) val memberId: Long,
    @Column(nullable = false) val cardId: Long,
    @Column(nullable = false) var quality: Int = 0,
    @Column(nullable = false) var easeFactor: Double = 2.5,
    @Column(name = "review_interval", nullable = false) var reviewInterval: Int = 0,
    @Column(nullable = false) var repetitions: Int = 0,
    @Column(nullable = false) var nextReviewDate: LocalDate = LocalDate.now(),
    @Column(nullable = false, updatable = false) val createdAt: Instant = Instant.now(),
    @Column(nullable = false) var updatedAt: Instant = Instant.now()
)
