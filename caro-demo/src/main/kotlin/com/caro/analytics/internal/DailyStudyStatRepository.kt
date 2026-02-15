package com.caro.analytics.internal

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional

internal interface DailyStudyStatRepository : JpaRepository<DailyStudyStat, DailyStudyStatId> {

    fun findByMemberIdAndDate(memberId: Long, date: LocalDate): Optional<DailyStudyStat>
}
