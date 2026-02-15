package com.caro.gamification.exception

sealed class GamificationException(message: String) : RuntimeException(message) {
    class StatsNotFoundException(memberId: Long) : GamificationException("Stats not found for member: $memberId")
}
