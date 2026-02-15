package com.caro.gamification

import java.time.Instant

data class MemberStatsDto(val memberId: Long, val totalExp: Long, val level: Int, val streakDays: Int)
data class BadgeDto(val id: Long, val name: String, val description: String, val earnedAt: Instant)
