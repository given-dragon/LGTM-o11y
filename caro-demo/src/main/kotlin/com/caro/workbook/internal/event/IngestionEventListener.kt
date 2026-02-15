package com.caro.workbook.internal.event

import com.caro.ingestion.IngestionCompletedEvent
import com.caro.workbook.internal.WorkbookServiceImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
internal class IngestionEventListener(
    private val workbookService: WorkbookServiceImpl
) {

    @ApplicationModuleListener
    fun onIngestionCompleted(event: IngestionCompletedEvent) {
        log.info { "Received IngestionCompletedEvent: jobId=${event.jobId}, cardCount=${event.extractedCards.size}" }
        val cardPairs = event.extractedCards.map { it.front to it.back }
        workbookService.createCardsBulk(event.deckId, cardPairs)
    }
}
