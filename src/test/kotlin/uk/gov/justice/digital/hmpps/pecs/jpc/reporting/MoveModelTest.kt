package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MoveModelTest{

    @Test
    fun `Move model without journeys should have a null price`(){
        val move = moveModel()
        assertThat(move.hasPrice()).isFalse
        assertThat(move.totalInPence()).isNull()
        assertThat(move.totalInPounds()).isNull()

    }

    @Test
    fun `Move model with a billable journey should be priced`(){
        val move = moveModel(journeys = mutableListOf(journeyModel()))
        assertThat(move.hasPrice()).isTrue
        assertThat(move.totalInPence()).isEqualTo(100)
        assertThat(move.totalInPounds()).isEqualTo(1.0)

    }
}