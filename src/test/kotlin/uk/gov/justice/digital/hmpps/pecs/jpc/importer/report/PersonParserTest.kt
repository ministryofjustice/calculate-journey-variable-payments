package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersonParserTest {

    @Test
    fun `Assert Person can be created from json`() {
        val personJson = """{"id":"PE1","updated_at": "2020-06-16T10:20:30+01:00","criminal_records_office":null,"nomis_prison_number":null,"police_national_computer":"83SHX5/YL","prison_number":"PRISON1","latest_nomis_booking_id":null,"gender":"male","age":100, "ethnicity" : "White American", "first_names" : "Billy the", "last_name": "Kid", "date_of_birth" : "1980-12-25"}
""".trimIndent()

        val parsedPerson = Person.fromJson(personJson)

        assertThat(reportPersonFactory()).isEqualTo(parsedPerson)
    }
}
