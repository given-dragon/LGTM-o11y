package com.caro.analytics

import java.time.LocalDate

/**
 * 일간 학습 통계 DTO.
 * 다른 모듈에 노출되는 읽기 전용 데이터 컨테이너.
 */
data class DailyStudyStatDto(
    val memberId: Long,
    val date: LocalDate,
    val totalCards: Int,
    val totalTimeMs: Long
)
