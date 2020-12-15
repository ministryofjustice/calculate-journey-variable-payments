package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

class ReportJourneyParserTest {

    @Test
    fun `Journey can be created from JSON with unknown state and unknown supplier`() {

        val from = fromPrisonNomisAgencyId()
        val to = notMappedNomisAgencyId()

        val journeyJson = """
            {"id":"J1", "updated_at": "2020-06-16T10:20:30+01:00", "move_id":"M1", "billable": false, "state":"WIBBLE", "supplier":"WOBBLE", "client_timestamp":"2020-06-16T10:20:30",
            "vehicle_registration":"UHE-92", "from_location":"WYI", "to_location":"NOT_MAPPED_AGENCY_ID"}
            """.trimIndent()

        val expectedJourney = reportJourneyFactory(fromLocation = from, toLocation = to)
        val parsedJourney = Journey.fromJson(journeyJson)

        assertThat(parsedJourney!!.fromNomisAgencyId).isEqualTo(expectedJourney.fromNomisAgencyId)
        assertThat(parsedJourney.toNomisAgencyId).isEqualTo(expectedJourney.toNomisAgencyId)
        assertThat(parsedJourney.vehicleRegistration).isEqualTo("UHE-92")
        assertThat(parsedJourney.state).isEqualTo(JourneyState.unknown)
        assertThat(parsedJourney.supplier).isEqualTo(Supplier.UNKNOWN)
    }

}