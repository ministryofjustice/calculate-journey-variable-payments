package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report

import uk.gov.justice.digital.hmpps.pecs.jpc.config.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import java.time.LocalDate
import java.util.Random

/**
 * Obfuscates people data loaded from reporting JSON files.
 */
class ObfuscatingReportImporter(
  provider: ReportingProvider,
  monitoringService: MonitoringService,
  reportReaderParser: ReportReaderParser
) : ReportImporter(provider, monitoringService, reportReaderParser) {

  override fun importPeopleOn(date: LocalDate): Sequence<Person> {
    return super.importPeopleOn(date).map { person ->
      person.copy(
        prisonNumber = randomString(10),
        firstNames = randomString(20),
        lastName = randomString(20),
        dateOfBirth = LocalDate.of(1800, 1, 1),
        ethnicity = "Other"
      )
    }.asSequence()
  }

  private fun randomString(length: Int) = ('a'..'z').randomString(length)

  private fun ClosedRange<Char>.randomString(length: Int) =
    (1..length)
      .map { (Random().nextInt(endInclusive.code - start.code) + start.code).toChar() }
      .joinToString("")
}
