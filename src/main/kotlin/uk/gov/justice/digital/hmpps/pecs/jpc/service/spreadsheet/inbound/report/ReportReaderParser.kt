package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Person
import java.io.Reader
import java.time.LocalDate
import java.util.Random

interface ReportReaderParser {
  fun <T> forEach(reportName: String, parser: (String) -> T?, consumer: (T) -> Unit)
}

/**
 * Looks up a report name using the supplied Reader which in turn provides one or more reporting entities to be parsed.
 * The parsed entities are then passed individually (as a Sequence of entities) to the consumer.
 */
class StandardReportReaderParser(private val reader: (String) -> Reader) : ReportReaderParser {
  override fun <T> forEach(reportName: String, parser: (String) -> T?, consumer: (T) -> Unit) {
    reader(reportName).forEachLine { parser(it)?.run(consumer) }
  }
}

/**
 * Obfuscates people (PII) data loaded from reporting JSON files. This is used if we need to test/load data locally.
 */
class ObfuscatingPiiReaderParser(private val reader: (String) -> Reader) : ReportReaderParser {

  override fun <T> forEach(reportName: String, parser: (String) -> T?, consumer: (T) -> Unit) {
    reader(reportName).forEachLine {
      parser(it)?.let { entity ->
        @Suppress("UNCHECKED_CAST")
        when (entity) {
          is Person -> entity.copy(
            prisonNumber = randomString(10),
            firstNames = randomString(20),
            lastName = randomString(20),
            dateOfBirth = LocalDate.of(1800, 1, 1),
            ethnicity = "Other"
          ) as T
          else -> entity
        }
      }?.run(consumer)
    }
  }

  private fun randomString(length: Int) = ('a'..'z').randomString(length)

  private fun ClosedRange<Char>.randomString(length: Int) =
    (1..length)
      .map { (Random().nextInt(endInclusive.code - start.code) + start.code).toChar() }
      .joinToString("")
}
