package com.caro.analytics

import java.time.LocalDate

/**
 * Analytics 모듈의 공개 API (Service Interface).
 * 다른 모듈에서 통계 데이터를 조회할 때 사용.
 */
interface AnalyticsService {

    /**
     * 특정 회원의 일간 학습 통계를 조회함.
     */
    fun getDailyStats(memberId: Long, date: LocalDate): DailyStudyStatDto?

    /**
     * 특정 회원의 오늘 학습한 카드 수를 조회함.
     * Notification 모듈에서 목표 달성 여부 판단에 사용.
     */
    fun getTodayCardCount(memberId: Long): Int
}
