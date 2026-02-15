package com.caro.workbook.internal

import com.caro.workbook.CreateCardCommand
import com.caro.workbook.DeckDto
import com.caro.workbook.exception.WorkbookException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.Optional

internal class WorkbookServiceTest : BehaviorSpec({
    val cardRepository = mockk<CardRepository>()
    val deckRepository = mockk<DeckRepository>()
    val workbookService = WorkbookServiceImpl(cardRepository, deckRepository)

    Given("카드 생성 요청이 들어왔을 때") {
        val deckId = 1L
        val memberId = 123L
        val command = CreateCardCommand(deckId, "Front", "Back", memberId)

        When("존재하지 않는 덱 ID라면") {
            every { deckRepository.existsById(deckId) } returns false

            Then("DeckNotFoundException이 발생해야 한다") {
                shouldThrow<WorkbookException.DeckNotFoundException> {
                    workbookService.createCard(command)
                }
            }
        }

        When("유효한 덱 ID라면") {
            every { deckRepository.existsById(deckId) } returns true
            every { cardRepository.save(any()) } answers {
                val card = firstArg<Card>()
                // Simulate generated ID
                Card(100L, card.deckId, card.front, card.back, Instant.now())
            }

            val result = workbookService.createCard(command)

            Then("카드가 저장되고 DTO가 반환되어야 한다") {
                result.id shouldBe 100L
                result.front shouldBe "Front"
                result.back shouldBe "Back"

                verify(exactly = 1) { cardRepository.save(any()) }
            }
        }
    }

    Given("덱 생성/조회 요청이 들어왔을 때") {
        val memberId = 1L
        
        When("새로운 덱을 생성하면") {
            every { deckRepository.save(any()) } answers {
                 val deck = firstArg<Deck>()
                 Deck(200L, deck.memberId, deck.name, deck.description, Instant.now())
            }
            
            val result = workbookService.createDeck(memberId, "My Deck", "Desc")

            Then("덱이 저장되고 정보가 반환되어야 한다") {
                result.id shouldBe 200L
                result.name shouldBe "My Deck"
                
                verify(exactly = 1) { deckRepository.save(any()) }
            }
        }

        When("특정 맴버의 덱 목록을 조회하면") {
             every { deckRepository.findByMemberId(memberId) } returns listOf(
                 Deck(200L, memberId, "My Deck", "Desc"),
                 Deck(201L, memberId, "Another Deck", null)
             )

             val result = workbookService.getDecksByMember(memberId)

             Then("해당 멤버가 소유한 모든 덱이 반환되어야 한다") {
                 result.size shouldBe 2
                 result[0].name shouldBe "My Deck"
                 result[1].name shouldBe "Another Deck"
             }
        }
    }
})
