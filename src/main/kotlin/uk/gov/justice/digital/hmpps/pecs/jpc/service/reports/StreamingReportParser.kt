package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Person
import java.io.Reader
import java.time.LocalDate
import java.util.Random

/**
 * Implementations of this interface will parse reports as sequence/stream to avoid consuming large amounts of memory.
 */
interface StreamingReportParser {
  fun <ENTITY> forEach(reportName: String, parse: (String) -> ENTITY?, consumer: (ENTITY) -> Unit)
}

/**
 * Looks up a report name using the supplied Reader which in turn provides one or more reporting entities to be parsed.
 * The parsed entities are then passed individually (as a Sequence of entities) to the consumer.
 */
class StandardStreamingReportParser(private val reader: (String) -> Reader) : StreamingReportParser {
  override fun <ENTITY> forEach(reportName: String, parse: (String) -> ENTITY?, consumer: (ENTITY) -> Unit) {
    reader(reportName).forEachLine { parse(it)?.run(consumer) }
  }
}

/**
 * Obfuscates people (PII) data loaded from reporting JSON files. This is used if we need to test/load data locally.
 */
class ObfuscatingStreamingReportParser(private val reader: (String) -> Reader) : StreamingReportParser {

  override fun <ENTITY> forEach(reportName: String, parse: (String) -> ENTITY?, consumer: (ENTITY) -> Unit) {
    reader(reportName).forEachLine {
      parse(it)?.let { entity ->
        @Suppress("UNCHECKED_CAST")
        when (entity) {
          is Person -> entity.copy(
            prisonNumber = randomString(10),
            firstNames = randomString(20),
            lastName = randomString(20),
            dateOfBirth = LocalDate.of(1800, 1, 1),
            ethnicity = "Other",
          ) as ENTITY
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
