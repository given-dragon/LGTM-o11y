package com.caro.gamification.internal

import org.springframework.data.jpa.repository.JpaRepository

internal interface EarnedBadgeRepository : JpaRepository<EarnedBadge, Long> {
    fun findByMemberId(memberId: Long): List<EarnedBadge>
    fun existsByMemberIdAndBadgeType(memberId: Long, badgeType: String): Boolean
}
