package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.moveM1
import java.math.BigDecimal

internal class ReportMoveTest {

  @Test
  fun `Move model without journeys should have a null price`() {
    val move = moveM1()
    assertThat(move.hasPrice()).isFalse
    assertThat(move.totalInPence()).isNull()
    assertThat(move.totalInPounds()).isNull()
  }

  @Test
  fun `Move model with a billable journey should be priced`() {
    val move = moveM1(journeys = listOf(journeyJ1()))
    assertThat(move.hasPrice()).isTrue
    assertThat(move.totalInPence()).isEqualTo(100)
    assertThat(move.totalInPounds()).isEqualTo(BigDecimal("1.00"))
  }
}
