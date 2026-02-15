package com.caro.ingestion.internal

import com.caro.ingestion.ExtractedCard
import com.caro.ingestion.IngestionCompletedEvent
import com.caro.ingestion.IngestionJobDto
import com.caro.ingestion.IngestionService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal enum class IngestionStatus { PENDING, PROCESSING, COMPLETED, FAILED }

internal data class IngestionJob(val jobId: String, val memberId: Long, val deckId: Long, var status: IngestionStatus = IngestionStatus.PENDING, var message: String? = null)

private val log = KotlinLogging.logger {}

@Service
internal class IngestionServiceImpl(private val eventPublisher: ApplicationEventPublisher) : IngestionService {
    private val jobs = ConcurrentHashMap<String, IngestionJob>()

    override fun submitIngestion(memberId: Long, deckId: Long, imageData: String): String {
        val jobId = UUID.randomUUID().toString()
        val job = IngestionJob(jobId, memberId, deckId)
        jobs[jobId] = job
        log.info { "Ingestion job submitted: jobId=$jobId" }
        processAsync(job, imageData)
        return jobId
    }

    override fun getJobStatus(jobId: String): IngestionJobDto? = jobs[jobId]?.let { IngestionJobDto(it.jobId, it.memberId, it.deckId, it.status.name, it.message) }

    @Async
    internal fun processAsync(job: IngestionJob, imageData: String) {
        try {
            job.status = IngestionStatus.PROCESSING
            Thread.sleep(2000)
            val extractedCards = listOf(ExtractedCard("Apple", "사과"), ExtractedCard("Banana", "바나나"), ExtractedCard("Cherry", "체리"))
            job.status = IngestionStatus.COMPLETED
            job.message = "Extracted ${extractedCards.size} cards"
            eventPublisher.publishEvent(IngestionCompletedEvent(job.jobId, job.memberId, job.deckId, extractedCards))
        } catch (e: Exception) {
            job.status = IngestionStatus.FAILED
            job.message = e.message
        }
    }
}
