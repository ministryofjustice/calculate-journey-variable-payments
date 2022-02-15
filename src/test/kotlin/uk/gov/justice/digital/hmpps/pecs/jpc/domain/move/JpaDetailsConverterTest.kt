package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JpaDetailsConverterTest {

  @Test
  fun `conversion to database column`() {
    assertThat(JpaDetailsConverter().convertToDatabaseColumn(Details(mapOf("key" to "value")))).isEqualTo("{\"key\": \"value\"}")
  }

  @Test
  fun `conversion to entity attribute`() {
    assertThat(JpaDetailsConverter().convertToEntityAttribute("{\"key\": \"value\"}")).isEqualTo(Details(mapOf("key" to "value")))
  }
}
