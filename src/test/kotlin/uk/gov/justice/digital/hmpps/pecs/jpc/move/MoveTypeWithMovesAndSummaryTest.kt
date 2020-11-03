package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MoveTypeWithMovesAndSummaryTest {

    val journey1 = journey()
    val movePriced1 = move(journeys = mutableListOf(journey1))
    val movePriced2 = move(journeys = mutableListOf(journey1)) // same priced journey as above

    val moveUnpriced = move(fromNomisAgencyId = "NEW")

    val moveTypeWithMovesAndSummary = MoveTypeWithMovesAndSummary(
            standard = MovesAndSummary(listOf(movePriced1), Summary(50.0, 10, 5, 100)),
            longHaul = MovesAndSummary(listOf(movePriced2), Summary(20.0, 5, 10, 200)),
            redirection = MovesAndSummary(listOf(moveUnpriced), Summary()),
            lockout = MovesAndSummary(listOf(), Summary()),
            multi = MovesAndSummary(listOf(), Summary()),
            cancelled = MovesAndSummary(listOf(), Summary())
    )

    @Test
    fun `summary of summaries`() {
        assertThat(moveTypeWithMovesAndSummary.summary()).isEqualTo(Summary(70.0, 15, 15, 300))
    }

    @Test
    fun `unique journeys`() {

        val uniqueJourneys = moveTypeWithMovesAndSummary.uniqueJourneys

        // 2 unique journeys
        assertThat(uniqueJourneys.journeys.map { it.fromNomisAgencyId }).containsExactlyInAnyOrder(movePriced1.fromNomisAgencyId, moveUnpriced.fromNomisAgencyId)

        // only one journey is unpriced
        assertThat(uniqueJourneys.countUnpriced()).isEqualTo(1)

    }
}