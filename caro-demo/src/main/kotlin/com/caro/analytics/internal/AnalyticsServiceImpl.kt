package com.caro.analytics.internal

import com.caro.analytics.AnalyticsService
import com.caro.analytics.DailyStudyStatDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
internal class AnalyticsServiceImpl(
    private val dailyStudyStatRepository: DailyStudyStatRepository
) : AnalyticsService {

    @Transactional(readOnly = true)
    override fun getDailyStats(memberId: Long, date: LocalDate): DailyStudyStatDto? {
        return dailyStudyStatRepository.findByMemberIdAndDate(memberId, date)
            .map { DailyStudyStatDto(it.memberId, it.date, it.totalCards, it.totalTimeMs) }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun getTodayCardCount(memberId: Long): Int {
        return dailyStudyStatRepository.findByMemberIdAndDate(memberId, LocalDate.now())
            .map { it.totalCards }
            .orElse(0)
    }

    /**
     * 학습 기록을 통계에 반영함 (내부 전용).
     * EventListener에서 호출됨.
     */
    @Transactional
    fun recordStudy(memberId: Long, reviewTimeMs: Long, studyDate: LocalDate) {
        val stat = dailyStudyStatRepository.findByMemberIdAndDate(memberId, studyDate)
            .orElseGet { DailyStudyStat(memberId = memberId, date = studyDate) }
        stat.addStudyRecord(reviewTimeMs)
        dailyStudyStatRepository.save(stat)
    }
}
