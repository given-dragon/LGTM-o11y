package com.caro.workbook.internal

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

internal class CardTest : DescribeSpec({

    describe("Card 엔티티") {
        it("기본값으로 생성 시 ID는 0이고 생성일시가 존재해야 한다") {
            val card = Card(
                deckId = 1L,
                front = "Question",
                back = "Answer"
            )

            card.id shouldBe 0L
            card.createdAt shouldNotBe null
        }

        it("앞면과 뒷면 내용을 수정할 수 있어야 한다") {
            val card = Card(
                deckId = 1L,
                front = "Q1",
                back = "A1"
            )

            card.front = "Updated Q"
            card.back = "Updated A"

            card.front shouldBe "Updated Q"
            card.back shouldBe "Updated A"
        }
    }
})
