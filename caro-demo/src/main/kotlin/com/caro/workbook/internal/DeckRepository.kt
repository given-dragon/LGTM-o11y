package com.caro.workbook.internal

import org.springframework.data.jpa.repository.JpaRepository

internal interface DeckRepository : JpaRepository<Deck, Long> {
    fun findByMemberId(memberId: Long): List<Deck>
}
