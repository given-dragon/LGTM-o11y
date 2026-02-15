package com.caro.notification.internal

import com.caro.notification.NotificationService
import com.caro.notification.NotificationType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

/**
 * Notification 서비스 구현체.
 * 현재는 로그로만 알림을 출력하며, 추후 FCM/APNs 연동 예정.
 */
private val log = KotlinLogging.logger {}

@Service
internal class NotificationServiceImpl : NotificationService {

    override fun sendNotification(memberId: Long, title: String, message: String, type: NotificationType) {
        // TODO: 실제 푸시 알림 연동 (Firebase Cloud Messaging 등)
        log.info { "[Notification] Sending to memberId=$memberId: [$type] $title - $message" }
    }
}
