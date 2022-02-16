package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Person
import java.io.StringReader
import java.time.LocalDate

class ObfuscatingPiiReaderParserTest {

  @Test
  fun `personal identifiable information is obfuscated`() {
    val prisonNumber = "PRISON1"
    val firstName = "Fred"
    val lastName = "Blogs"
    val ethnicity = "European"

    ObfuscatingPiiReaderParser {
      StringReader(
        personProvider(
          prisonNumber = prisonNumber,
          firstName = firstName,
          lastName = lastName,
          ethnicity = ethnicity
        )
      )
    }.forEach("_", { Person.fromJson(it) }) { obfuscatedPerson ->
      assertThat(obfuscatedPerson.dateOfBirth).isEqualTo(LocalDate.of(1800, 1, 1))
      assertThat(obfuscatedPerson.prisonNumber?.trim()?.uppercase()).isNotEqualTo(prisonNumber.trim().uppercase())
      assertThat(obfuscatedPerson.firstNames?.trim()?.uppercase()).isNotEqualTo(firstName.trim().uppercase())
      assertThat(obfuscatedPerson.lastName?.trim()?.uppercase()).isNotEqualTo(lastName.trim().uppercase())
      assertThat(obfuscatedPerson.ethnicity).isEqualTo("Other")
    }
  }

  private fun personProvider(prisonNumber: String, firstName: String, lastName: String, ethnicity: String) =
    """{"id":"PE1","updated_at": "2020-06-16T10:20:30+01:00", "prison_number":"$prisonNumber","latest_nomis_booking_id":null,"gender":"male","age":100, "ethnicity" : "$ethnicity", "first_names" : "$firstName", "last_name": "$lastName", "date_of_birth" : "1980-12-25"}""".trimIndent()
}
