package com.caro.notification.internal.event

import com.caro.analytics.AnalyticsService
import com.caro.notification.NotificationService
import com.caro.notification.NotificationType
import com.caro.review.event.CardReviewedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * CardReviewedEvent를 구독하여 목표 달성 시 알림을 발송하는 리스너.
 * Analytics 모듈에 오늘 학습량을 조회하여 목표치와 비교함.
 */
private val log = KotlinLogging.logger {}

@Component
internal class GoalAchievementEventListener(
    private val analyticsService: AnalyticsService,
    private val notificationService: NotificationService,
    @Value("\${caro.daily-goal.cards:20}") private val dailyGoalCards: Int
) {
    @Async("eventExecutor")
    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onCardReviewed(event: CardReviewedEvent) {
        try {
            val todayCount = analyticsService.getTodayCardCount(event.memberId)
            log.debug { "[Notification] memberId=${event.memberId} studied $todayCount cards today (goal=$dailyGoalCards)" }

            // 정확히 목표 달성 시점에만 알림 발송 (이미 달성한 경우 중복 발송 방지)
            if (todayCount == dailyGoalCards) {
                notificationService.sendNotification(
                    memberId = event.memberId,
                    title = "🎉 오늘의 목표 달성!",
                    message = "대단해요! 오늘 목표인 ${dailyGoalCards}장을 모두 학습했습니다.",
                    type = NotificationType.GOAL_ACHIEVED
                )
            }
        } catch (ex: Exception) {
            log.error(ex) { "[Notification] Failed to check goal achievement: ${ex.message}" }
            // Best Effort: 로그만 남김
        }
    }
}
