package com.caro.workbook.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "cards")
internal class Card(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false) val deckId: Long,
    @Column(nullable = false, length = 2000) var front: String,
    @Column(nullable = false, length = 2000) var back: String,
    @Column(nullable = false, updatable = false) val createdAt: Instant = Instant.now()
)
