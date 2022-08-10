package uk.gov.justice.digital.hmpps.pecs.jpc.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.util.json.dateFieldFromJson
import java.time.LocalDate

class JsonFunctionsTests {

  @Test
  fun `returns date value from json string`() {
    assertThat(dateFieldFromJson("""{ "date": "2022-08-03"}""", "date")).isEqualTo(LocalDate.of(2022, 8, 3))
  }

  @Test
  fun `returns null when no matching field in json string`() {
    assertThat(dateFieldFromJson("""{ "date": "2022-08-03"}""", "daate")).isNull()
  }
}
