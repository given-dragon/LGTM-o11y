package com.caro.gamification.internal.web

import com.caro.gamification.BadgeDto
import com.caro.gamification.GamificationService
import com.caro.gamification.MemberStatsDto
import com.caro.shared.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/gamification")
class GamificationController(private val gamificationService: GamificationService) {
    @GetMapping("/stats/{memberId}")
    fun getMemberStats(@PathVariable memberId: Long): ApiResponse<MemberStatsDto> = ApiResponse.ok(gamificationService.getMemberStats(memberId))

    @GetMapping("/badges/{memberId}")
    fun getEarnedBadges(@PathVariable memberId: Long): ApiResponse<List<BadgeDto>> = ApiResponse.ok(gamificationService.getEarnedBadges(memberId))
}
