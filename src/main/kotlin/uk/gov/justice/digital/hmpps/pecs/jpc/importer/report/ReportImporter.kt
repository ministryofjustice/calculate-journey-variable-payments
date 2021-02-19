package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import java.time.LocalDate
import kotlin.streams.toList

@Component
class ReportImporter(
  @Autowired val provider: ReportingProvider,
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun importMovesJourneysEventsOn(date: LocalDate) = importMovesJourneysEvents(date, date)

  private fun importMovesJourneysEvents(from: LocalDate, to: LocalDate): Collection<Move> {
    val movesContent = getContents("moves", from, to)
    val journeysContent = getContents("journeys", from, to)
    val eventsContent = getContents("events", from, to)
    return ReportParser.parseMovesJourneysEvents(
      moveFiles = movesContent,
      journeyFiles = journeysContent,
      eventFiles = eventsContent,
    )
  }

  fun importPeopleOn(date: LocalDate) = importPeople(date, date)

  private fun importPeople(from: LocalDate, to: LocalDate): Sequence<Person> {
    val peopleContent = getContents("people", from, to)
    return ReportParser.parseAsPerson(peopleFiles = peopleContent)
  }

  fun importProfilesOn(date: LocalDate) = importProfiles(date, date)

  private fun importProfiles(from: LocalDate, to: LocalDate): Sequence<Profile> {
    val profilesContent = getContents("profiles", from, to)
    return ReportParser.parseAsProfile(profileFiles = profilesContent)
  }

  private fun getContents(entity: String, from: LocalDate, to: LocalDate): List<String> {
    logger.info("Getting $entity between $from and $to")
    val fileNames = fileNamesForDate(entity, from, to)
    return fileNames.mapNotNull {
      try {
        logger.info("Retrieving file $it")
        provider.get(it)
      } catch (e: Exception) {
        logger.warn("Error attempting to get file $it, exception: $e")
        null
      }
    }
  }

  companion object {
    fun fileNamesForDate(entity: String, from: LocalDate, to: LocalDate): List<String> {
      return from.datesUntil(to.plusDays(1)).map { d ->
        "${d.year}/${padZero(d.monthValue)}/${padZero(d.dayOfMonth)}/${d.year}-${padZero(d.monthValue)}-${padZero(d.dayOfMonth)}-$entity.jsonl"
      }.toList()
    }

    private fun padZero(value: Int) = if (value < 10) "0$value" else value.toString()
  }
}
