package uk.gov.justice.digital.hmpps.pecs.jpc.service.moves

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyWithPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate

internal class JourneyServiceTest {

  private val startOfMonth = LocalDate.now().startOfMonth()

  private val endOfMonth = startOfMonth.endOfMonth()

  private val journeyQueryRepository = mock<JourneyQueryRepository>()

  private val service = JourneyService(journeyQueryRepository)

  @Test
  fun `un-priced journeys with agency IDs only are ordered alphabetically`() {
    whenever(
      journeyQueryRepository.distinctJourneysAndPriceInDateRange(
        Supplier.SERCO,
        startOfMonth,
        endOfMonth,
        true,
      ),
    ).thenReturn(
      listOf(
        journey(fromAgencyId = "C", toAgencyId = "D"),
        journey(fromAgencyId = "C", toAgencyId = "A"),
        journey(fromAgencyId = "C", toAgencyId = "B"),
        journey(fromAgencyId = "A", toAgencyId = "D"),
        journey(fromAgencyId = "Z", toAgencyId = "D"),
        journey(fromAgencyId = "Z", toAgencyId = "X"),
      ),
    )

    assertThat(service.distinctJourneysExcludingPriced(Supplier.SERCO, startOfMonth)).containsExactly(
      journey(fromAgencyId = "A", toAgencyId = "D"),
      journey(fromAgencyId = "C", toAgencyId = "A"),
      journey(fromAgencyId = "C", toAgencyId = "B"),
      journey(fromAgencyId = "C", toAgencyId = "D"),
      journey(fromAgencyId = "Z", toAgencyId = "D"),
      journey(fromAgencyId = "Z", toAgencyId = "X"),
    )
  }

  @Test
  fun `un-priced journeys with combination of agency IDs and site names are ordered alphabetically`() {
    whenever(
      journeyQueryRepository.distinctJourneysAndPriceInDateRange(
        Supplier.SERCO,
        startOfMonth,
        endOfMonth,
        true,
      ),
    ).thenReturn(
      listOf(
        journey(fromAgencyId = "C", toAgencyId = "D"),
        journey(fromAgencyId = "C", toAgencyId = "A"),
        journey(fromAgencyId = "C", toAgencyId = "B"),
        journey(fromAgencyId = "A", toAgencyId = "D"),
        journey(fromAgencyId = "X", fromSiteName = "A SITE NAME", "Z"),
        journey(fromAgencyId = "Z", fromSiteName = "A", "X", "A SITE NAME"),
      ),
    )

    assertThat(service.distinctJourneysExcludingPriced(Supplier.SERCO, startOfMonth)).containsExactly(
      journey(fromAgencyId = "X", fromSiteName = "A SITE NAME", toAgencyId = "Z"),
      journey(fromAgencyId = "Z", fromSiteName = "A", toAgencyId = "X", toSiteName = "A SITE NAME"),
      journey(fromAgencyId = "A", toAgencyId = "D"),
      journey(fromAgencyId = "C", toAgencyId = "A"),
      journey(fromAgencyId = "C", toAgencyId = "B"),
      journey(fromAgencyId = "C", toAgencyId = "D"),
    )
  }

  private fun LocalDate.startOfMonth() = this.withDayOfMonth(1)

  private fun LocalDate.endOfMonth() = this.withDayOfMonth(1).plusMonths(1).minusDays(1)

  private fun journey(
    fromAgencyId: String,
    fromSiteName: String? = null,
    toAgencyId: String,
    toSiteName: String? = null,
  ) =
    JourneyWithPrice(fromAgencyId, null, fromSiteName, toAgencyId, null, toSiteName, null, null, null)
}
