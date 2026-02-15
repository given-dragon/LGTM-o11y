package com.caro.workbook.exception

sealed class WorkbookException(message: String) : RuntimeException(message) {
    class CardNotFoundException(cardId: Long) : WorkbookException("Card not found: $cardId")
    class DeckNotFoundException(deckId: Long) : WorkbookException("Deck not found: $deckId")
    class InvalidDeckException(message: String) : WorkbookException(message)
}
