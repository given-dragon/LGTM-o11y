package com.caro.workbook

data class CreateCardCommand(
    val deckId: Long,
    val front: String,
    val back: String,
    val memberId: Long
)

