package com.caro.gamification

interface GamificationService {
    fun getMemberStats(memberId: Long): MemberStatsDto
    fun getEarnedBadges(memberId: Long): List<BadgeDto>
}

