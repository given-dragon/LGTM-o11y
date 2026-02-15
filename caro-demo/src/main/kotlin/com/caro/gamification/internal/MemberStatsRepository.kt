package com.caro.gamification.internal

import org.springframework.data.jpa.repository.JpaRepository

internal interface MemberStatsRepository : JpaRepository<MemberStats, Long>
