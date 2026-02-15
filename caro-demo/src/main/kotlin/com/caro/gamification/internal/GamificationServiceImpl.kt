package com.caro.gamification.internal

import com.caro.gamification.BadgeDto
import com.caro.gamification.GamificationService
import com.caro.gamification.MemberStatsDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Service
internal class GamificationServiceImpl(
    private val memberStatsRepository: MemberStatsRepository,
    private val earnedBadgeRepository: EarnedBadgeRepository
) : GamificationService {

    @Transactional(readOnly = true)
    override fun getMemberStats(memberId: Long): MemberStatsDto {
        val stats = memberStatsRepository.findById(memberId).orElse(MemberStats(memberId = memberId))
        return MemberStatsDto(stats.memberId, stats.totalExp, stats.level, stats.streakDays)
    }

    @Transactional(readOnly = true)
    override fun getEarnedBadges(memberId: Long): List<BadgeDto> = earnedBadgeRepository.findByMemberId(memberId).map { BadgeDto(it.id, it.name, it.description, it.earnedAt) }

    @Transactional
    fun addExpAndUpdateStreak(memberId: Long, exp: Long, today: LocalDate) {
        val stats = memberStatsRepository.findById(memberId).orElseGet { MemberStats(memberId = memberId) }
        val previousLevel = stats.level
        stats.addExp(exp); stats.updateStreak(today)
        memberStatsRepository.save(stats)
        checkAndAwardBadges(memberId, stats, previousLevel)
    }

    private fun checkAndAwardBadges(memberId: Long, stats: MemberStats, previousLevel: Int) {
        if (stats.totalExp > 0 && !hasBadge(memberId, "FIRST_REVIEW")) awardBadge(memberId, "FIRST_REVIEW", "첫 복습!", "첫 번째 복습을 완료했습니다")
        if (stats.level >= 5 && previousLevel < 5 && !hasBadge(memberId, "LEVEL_5")) awardBadge(memberId, "LEVEL_5", "레벨 5 달성", "레벨 5에 도달했습니다")
        if (stats.streakDays >= 7 && !hasBadge(memberId, "STREAK_7")) awardBadge(memberId, "STREAK_7", "7일 연속 학습", "7일 연속으로 학습했습니다")
    }

    private fun hasBadge(memberId: Long, badgeType: String) = earnedBadgeRepository.existsByMemberIdAndBadgeType(memberId, badgeType)
    private fun awardBadge(memberId: Long, badgeType: String, name: String, description: String) {
        earnedBadgeRepository.save(EarnedBadge(memberId = memberId, badgeType = badgeType, name = name, description = description))
        log.info { "Badge awarded: memberId=$memberId, badge=$name" }
    }
}
