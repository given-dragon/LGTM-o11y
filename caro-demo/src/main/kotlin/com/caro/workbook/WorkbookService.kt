package com.caro.workbook

interface WorkbookService {
    fun createCard(command: CreateCardCommand): CardDto
    fun getCard(cardId: Long): CardDto?
    fun getCardsByDeckId(deckId: Long): List<CardDto>
    fun deleteCard(cardId: Long)
    fun createDeck(memberId: Long, name: String, description: String?): DeckDto
    fun getDecksByMember(memberId: Long): List<DeckDto>
    fun getDeck(deckId: Long): DeckDto?
    fun deleteDeck(deckId: Long)
    fun createCardsBulk(deckId: Long, cards: List<Pair<String, String>>): List<CardDto>
}
