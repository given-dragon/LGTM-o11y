package com.caro.analytics.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * 일간 학습 통계 엔티티.
 * 복합 키(memberId + date)로 구성되어 일별로 통계를 집계함.
 */
@Entity
@Table(name = "daily_study_stat")
@IdClass(DailyStudyStatId::class)
internal class DailyStudyStat(
    @Id
    @Column(name = "member_id")
    val memberId: Long,

    @Id
    @Column(name = "study_date")
    val date: LocalDate,

    @Column(name = "total_cards")
    var totalCards: Int = 0,

    @Column(name = "total_time_ms")
    var totalTimeMs: Long = 0L
) {
    fun addStudyRecord(reviewTimeMs: Long) {
        this.totalCards++
        this.totalTimeMs += reviewTimeMs
    }
}

/**
 * 복합 키 클래스.
 */
internal data class DailyStudyStatId(
    val memberId: Long = 0L,
    val date: LocalDate = LocalDate.now()
) : java.io.Serializable
