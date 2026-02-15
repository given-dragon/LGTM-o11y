package com.caro.gamification.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "member_stats")
internal class MemberStats(
    @Id val memberId: Long,
    @Column(nullable = false) var totalExp: Long = 0,
    @Column(nullable = false) var level: Int = 1,
    @Column(nullable = false) var streakDays: Int = 0,
    var lastStudyDate: LocalDate? = null,
    @Column(nullable = false) var updatedAt: Instant = Instant.now()
) {
    fun addExp(exp: Long) { totalExp += exp; level = ((totalExp / 100) + 1).toInt().coerceAtLeast(1); updatedAt = Instant.now() }
    fun updateStreak(today: LocalDate) {
        streakDays = when { lastStudyDate == null -> 1; lastStudyDate == today.minusDays(1) -> streakDays + 1; lastStudyDate == today -> streakDays; else -> 1 }
        lastStudyDate = today; updatedAt = Instant.now()
    }
}
