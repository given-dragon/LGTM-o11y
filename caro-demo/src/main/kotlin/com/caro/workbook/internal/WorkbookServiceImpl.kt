package com.caro.workbook.internal

import com.caro.workbook.CardDto
import com.caro.workbook.CreateCardCommand
import com.caro.workbook.DeckDto
import com.caro.workbook.WorkbookService
import com.caro.workbook.exception.WorkbookException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class WorkbookServiceImpl(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository
) : WorkbookService {

    @Transactional
    override fun createCard(command: CreateCardCommand): CardDto {
        if (!deckRepository.existsById(command.deckId)) throw WorkbookException.DeckNotFoundException(command.deckId)
        return cardRepository.save(Card(deckId = command.deckId, front = command.front, back = command.back)).toDto()
    }

    @Transactional(readOnly = true)
    override fun getCard(cardId: Long): CardDto? = cardRepository.findById(cardId).map { it.toDto() }.orElse(null)

    @Transactional(readOnly = true)
    override fun getCardsByDeckId(deckId: Long): List<CardDto> = cardRepository.findByDeckId(deckId).map { it.toDto() }

    @Transactional
    override fun deleteCard(cardId: Long) {
        if (!cardRepository.existsById(cardId)) throw WorkbookException.CardNotFoundException(cardId)
        cardRepository.deleteById(cardId)
    }

    @Transactional
    override fun createDeck(memberId: Long, name: String, description: String?): DeckDto =
        deckRepository.save(Deck(memberId = memberId, name = name, description = description)).toDto()

    @Transactional(readOnly = true)
    override fun getDecksByMember(memberId: Long): List<DeckDto> = deckRepository.findByMemberId(memberId).map { it.toDto() }

    @Transactional(readOnly = true)
    override fun getDeck(deckId: Long): DeckDto? = deckRepository.findById(deckId).map { it.toDto() }.orElse(null)

    @Transactional
    override fun deleteDeck(deckId: Long) {
        if (!deckRepository.existsById(deckId)) throw WorkbookException.DeckNotFoundException(deckId)
        cardRepository.deleteByDeckId(deckId)
        deckRepository.deleteById(deckId)
    }

    @Transactional
    override fun createCardsBulk(deckId: Long, cards: List<Pair<String, String>>): List<CardDto> {
        if (!deckRepository.existsById(deckId)) throw WorkbookException.DeckNotFoundException(deckId)
        return cards.map { (front, back) -> cardRepository.save(Card(deckId = deckId, front = front, back = back)).toDto() }
    }

    private fun Card.toDto() = CardDto(id, deckId, front, back, createdAt)
    private fun Deck.toDto() = DeckDto(id, memberId, name, description, createdAt)
}
