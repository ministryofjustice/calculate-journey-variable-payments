package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import java.time.LocalDate
import kotlin.streams.toList

@Component
class ReportImporter(
        @Autowired val provider: ReportingProvider,
        @Autowired val timeSource: TimeSource) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun importMovesJourneysEvents(from: LocalDate, to: LocalDate = timeSource.date()): Collection<Report>{
        val movesContent = getContents("moves", from, to)
        val journeysContent = getContents("journeys", from, to)
        val eventsContent = getContents("events", from, to)
        return ReportParser.parseMovesJourneysEvents(
                moveFiles = movesContent,
                journeyFiles = journeysContent,
                eventFiles = eventsContent,
        )
    }

    fun importProfilesPeople(from: LocalDate, to: LocalDate = timeSource.date()): Sequence<Person>{
        val profilesContent = getContents("profiles", from, to)
        val peopleContent = getContents("people", from, to)
        return ReportParser.parsePeople( profileFiles = profilesContent, peopleFiles = peopleContent)
    }

    private fun getContents(entity: String, from: LocalDate, to: LocalDate): List<String>{
        logger.info("Getting $entity between $from and $to")
        val fileNames = fileNamesForDate(entity, from, to)
        return fileNames.map {
            try {
                logger.info("Retrieving file $it")
                provider.get(it)
            }
            catch (e: Exception){
                logger.warn("Error attempting to get file $it, exception: ${e.toString()}")
                null
            }
        }.filterNotNull()
    }

    companion object{
        fun fileNamesForDate(entity: String, from: LocalDate, to: LocalDate): List<String> {
            return from.datesUntil(to.plusDays(1)).map {d ->
                "${d.year}/${padZero(d.monthValue)}/${padZero(d.dayOfMonth)}/${d.year}-${padZero(d.monthValue)}-${padZero(d.dayOfMonth)}-$entity.jsonl"
            }.toList()

        }

        private fun padZero(value: Int) = if (value < 10) "0${value}" else value.toString()
    }

}
