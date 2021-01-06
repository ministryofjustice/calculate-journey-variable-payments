package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.move.moveM1

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
    assertThat(move.totalInPounds()).isEqualTo(1.0)
  }
}
