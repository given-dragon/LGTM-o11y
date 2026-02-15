package com.caro.workbook.internal.web

import jakarta.validation.constraints.NotBlank

data class CreateDeckRequest(
    val memberId: Long,
    @field:NotBlank val name: String,
    val description: String? = null
)

data class CreateCardRequest(
    val memberId: Long,
    val deckId: Long,
    @field:NotBlank val front: String,
    @field:NotBlank val back: String
)
