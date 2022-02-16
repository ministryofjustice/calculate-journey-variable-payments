package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class PersonTest {

  @Test
  fun `PII is not exposed via toString`() {
    val person = Person(
      personId = "P1",
      updatedAt = LocalDateTime.now(),
      prisonNumber = "111111",
      latestNomisBookingId = 222222,
      firstNames = "LUKE",
      lastName = "SKYWALKER",
      dateOfBirth = LocalDate.of(1980, 4, 28),
      gender = "MALE",
      ethnicity = "JEDI"
    )

    assertThat(person.toString().uppercase())
      .doesNotContain(
        "111111",
        222222.toString(),
        "LUKE",
        "SKYWALKER",
        LocalDate.of(1980, 4, 28).toString(),
        "MALE",
        "JEDI"
      )
  }
}
