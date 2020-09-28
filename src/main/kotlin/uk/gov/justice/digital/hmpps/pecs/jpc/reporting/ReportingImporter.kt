package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.service.Importer
import java.time.LocalDate

@Component
class ReportingImporter : Importer<Collection<MovePersonJourneysEvents>> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var provider: ReportingProvider

    override fun import() = import(LocalDate.now().minusDays(1))

    fun import(date: LocalDate, startDayOfMonth: Int = 1): Collection<MovePersonJourneysEvents>{
        val movesContent = getContents("moves", date, startDayOfMonth)
        val journeysContent = getContents("journeys", date, startDayOfMonth)
        val eventsContent = getContents("events", date, startDayOfMonth)
        val profilesContent = getContents("profiles", date, startDayOfMonth)
        val peopleContent = getContents("people", date, startDayOfMonth)
        return ReportingParser.parseAll(
                moveFiles = movesContent,
                journeyFiles = journeysContent,
                eventFiles = eventsContent,
                profileFiles = profilesContent,
                peopleFiles = peopleContent)
    }

    private fun getContents(entity: String, date: LocalDate, startDayOfMonth: Int): List<String>{
        // TODO better validation / error handling - it just currently logs the error and ignores it
        val fileNames = fileNamesForDate(entity, date, startDayOfMonth)
        return fileNames.map {
            try {
                logger.info("Retrieveing file $it")
                provider.get(it)
            }
            catch (e: Exception){
                logger.warn("Error attempting to get file $it, exception: ${e.toString()}")
                null
            }
        }.filterNotNull()
    }

    companion object{
        fun fileNamesForDate(entity: String, endDate: LocalDate, startDayOfMonth: Int): List<String> {
            return (startDayOfMonth..endDate.dayOfMonth).map {
                day -> "${endDate.year}/${padZero(endDate.monthValue)}/${padZero(day)}/${endDate.year}-${padZero(endDate.monthValue)}-${padZero(day)}-$entity.jsonl"
            }
        }

        private fun padZero(value: Int) = if (value < 10) "0${value}" else value.toString()
    }

}
