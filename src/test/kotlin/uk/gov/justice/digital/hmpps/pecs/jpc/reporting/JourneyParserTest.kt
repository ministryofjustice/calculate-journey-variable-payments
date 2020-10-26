package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JourneyParserTest {

    @Test
    fun `Assert Journey can be created from json`() {

        val from = fromPrisonNomisAgencyId()
        val to = notMappedNomisAgencyId()

        val journeyJson = """
            {"id":"J1", "move_id":"M1", "billable": false, "state":"completed", "supplier":"serco", "client_timestamp":"2020-06-16T10:20:30",
            "vehicle_registration":"UHE-92", "from_location":"WYI", "to_location":"NOT_MAPPED_AGENCY_ID"}
            """.trimIndent()

        val expectedJourney = journeyFactory(fromLocation = from, toLocation = to)
        val parsedJourney = Journey.fromJson(journeyJson)

        assertThat(parsedJourney!!.fromNomisAgencyId).isEqualTo(expectedJourney.fromNomisAgencyId)
        assertThat(parsedJourney.toNomisAgencyId).isEqualTo(expectedJourney.toNomisAgencyId)
        assertThat(parsedJourney.vehicleRegistration).isEqualTo("UHE-92")

    }

}