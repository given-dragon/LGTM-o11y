package com.caro.workbook.internal.web

import com.caro.shared.ApiResponse
import com.caro.workbook.CardDto
import com.caro.workbook.CreateCardCommand
import com.caro.workbook.DeckDto
import com.caro.workbook.WorkbookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workbook")
class WorkbookController(
    private val workbookService: WorkbookService
) {
    @PostMapping("/decks")
    @ResponseStatus(HttpStatus.CREATED)
    fun createDeck(@Valid @RequestBody request: CreateDeckRequest): ApiResponse<DeckDto> {
        return ApiResponse.ok(workbookService.createDeck(request.memberId, request.name, request.description))
    }

    @GetMapping("/decks")
    fun getDecksByMember(@RequestParam memberId: Long): ApiResponse<List<DeckDto>> {
        return ApiResponse.ok(workbookService.getDecksByMember(memberId))
    }

    @GetMapping("/decks/{deckId}")
    fun getDeck(@PathVariable deckId: Long): ApiResponse<DeckDto> {
        val deck = workbookService.getDeck(deckId)
            ?: return ApiResponse.error("NOT_FOUND", "Deck not found: $deckId")
        return ApiResponse.ok(deck)
    }

    @DeleteMapping("/decks/{deckId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDeck(@PathVariable deckId: Long) = workbookService.deleteDeck(deckId)

    @PostMapping("/cards")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCard(@Valid @RequestBody request: CreateCardRequest): ApiResponse<CardDto> {
        return ApiResponse.ok(workbookService.createCard(
            CreateCardCommand(request.deckId, request.front, request.back, request.memberId)
        ))
    }

    @GetMapping("/decks/{deckId}/cards")
    fun getCardsByDeck(@PathVariable deckId: Long): ApiResponse<List<CardDto>> {
        return ApiResponse.ok(workbookService.getCardsByDeckId(deckId))
    }

    @GetMapping("/cards/{cardId}")
    fun getCard(@PathVariable cardId: Long): ApiResponse<CardDto> {
        val card = workbookService.getCard(cardId)
            ?: return ApiResponse.error("NOT_FOUND", "Card not found: $cardId")
        return ApiResponse.ok(card)
    }

    @DeleteMapping("/cards/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCard(@PathVariable cardId: Long) = workbookService.deleteCard(cardId)
}


