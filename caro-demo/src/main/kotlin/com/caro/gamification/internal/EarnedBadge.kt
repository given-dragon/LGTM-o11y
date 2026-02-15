package com.caro.gamification.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "earned_badges")
internal class EarnedBadge(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    @Column(nullable = false) val memberId: Long,
    @Column(nullable = false) val badgeType: String,
    @Column(nullable = false) val name: String,
    @Column(nullable = false) val description: String,
    @Column(nullable = false, updatable = false) val earnedAt: Instant = Instant.now()
)
