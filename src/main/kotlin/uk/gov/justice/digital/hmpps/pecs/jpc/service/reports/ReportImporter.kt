package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Profile
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

private val logger = loggerFor<ReportImporter>()

open class ReportImporter(
  @Autowired private val provider: ReportingProvider,
  @Autowired private val monitoringService: MonitoringService,
  @Autowired private val streamingReportParser: StreamingReportParser,
) {

  open fun importMovesJourneysEventsOn(date: LocalDate) = importMovesJourneysEvents(date, date)

  private fun importMovesJourneysEvents(from: LocalDate, to: LocalDate): Collection<Move> {
    val movesContent = getContents("moves", from, to)

    val journeysContent = getContents("journeys", from, to)

    val eventsContent = getContents("events", from, to)

    return InMemoryReportParser.parseMovesJourneysEvents(
      moveFiles = movesContent,
      journeyFiles = journeysContent,
      eventFiles = eventsContent,
    )
  }

  private fun getContents(entity: String, from: LocalDate, to: LocalDate): List<String> {
    logger.info("Getting $entity between $from and $to")
    val fileNames = fileNamesForDate(entity, from, to)
    return fileNames.mapNotNull {
      try {
        logger.info("Retrieving file $it")
        provider.get(it)
      } catch (e: Exception) {
        logger.error("The service is missing data which may affect pricing due to missing file $it. Exception: $e")
        monitoringService.capture("The service is missing data which may affect pricing due to missing file $it. Exception: ${e.message}")
        null
      }
    }
  }

  open fun importPeople(date: LocalDate, consumer: (Person) -> Unit) {
    val fileNameForDate = fileNamesForDate("people", date, date).first()

    Result.runCatching {
      streamingReportParser.forEach(fileNameForDate, { Person.fromJson(it) }) { consumer(it) }
    }.onFailure {
      monitoringService.capture("Error processing people file $fileNameForDate, exception: ${it.message}")
    }
  }

  fun importProfiles(date: LocalDate, consumer: (Profile) -> Unit) {
    val fileNameForDate = fileNamesForDate("profiles", date, date).first()

    Result.runCatching {
      streamingReportParser.forEach(fileNameForDate, { Profile.fromJson(it) }) { consumer(it) }
    }.onFailure {
      monitoringService.capture("Error processing profiles file $fileNameForDate, exception: ${it.message}")
    }
  }

  companion object {
    fun fileNamesForDate(entity: String, from: LocalDate, to: LocalDate): List<String> {
      return from.datesUntil(to.plusDays(1)).map { d ->
        "${d.year}/${padZero(d.monthValue)}/${padZero(d.dayOfMonth)}/${d.year}-${padZero(d.monthValue)}-${padZero(d.dayOfMonth)}-$entity.jsonl"
      }.toList()
    }

    fun reportFilenamesFor(date: LocalDate) =
      listOf("moves", "events", "journeys", "profiles", "people").flatMap { entity -> fileNamesForDate(entity, date, date) }

    private fun padZero(value: Int) = if (value < 10) "0$value" else value.toString()
  }
}
