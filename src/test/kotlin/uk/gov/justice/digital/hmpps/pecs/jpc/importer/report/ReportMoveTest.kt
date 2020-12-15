package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.move.journey
import uk.gov.justice.digital.hmpps.pecs.jpc.move.move

internal class ReportMoveTest{

    @Test
    fun `Move model without journeys should have a null price`(){
        val move = move()
        assertThat(move.hasPrice()).isFalse
        assertThat(move.totalInPence()).isNull()
        assertThat(move.totalInPounds()).isNull()

    }

    @Test
    fun `Move model with a billable journey should be priced`(){
        val move = move(journeys = listOf(journey()))
        assertThat(move.hasPrice()).isTrue
        assertThat(move.totalInPence()).isEqualTo(100)
        assertThat(move.totalInPounds()).isEqualTo(1.0)

    }
}