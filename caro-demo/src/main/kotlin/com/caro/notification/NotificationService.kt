package com.caro.notification

/**
 * Notification 모듈의 공개 API.
 * 푸시 알림, 이메일 등 다양한 채널로 알림 발송.
 */
interface NotificationService {

    /**
     * 특정 회원에게 알림을 발송함.
     * @param memberId 대상 회원 ID
     * @param title 알림 제목
     * @param message 알림 내용
     * @param type 알림 유형 (GOAL_ACHIEVED, DECK_COMPLETED, BADGE_EARNED 등)
     */
    fun sendNotification(memberId: Long, title: String, message: String, type: NotificationType)
}

enum class NotificationType {
    GOAL_ACHIEVED,
    DECK_COMPLETED,
    BADGE_EARNED,
    STREAK_REMINDER,
    SYSTEM
}
