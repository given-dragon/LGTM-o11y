package com.caro.workbook.internal

import org.springframework.data.jpa.repository.JpaRepository

internal interface CardRepository : JpaRepository<Card, Long> {
    fun findByDeckId(deckId: Long): List<Card>
    fun deleteByDeckId(deckId: Long)
}
