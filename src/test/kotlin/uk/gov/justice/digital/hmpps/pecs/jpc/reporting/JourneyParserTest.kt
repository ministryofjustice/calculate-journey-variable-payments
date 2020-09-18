package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JourneyParserTest {

    @Test
    fun `Assert Journey can be created from json`() {
        println(cannedJourney())
        val journeyJson = """
            {"id":"J1", "move_id":"M1", "billable": false, "state":"completed", "supplier":"serco", "client_timestamp":"2020-06-16T10:20:30",
            "vehicle_registration":"UHE-92", "from_location":"GCS11", "to_location":"HPS008"}
            """.trimIndent()
        val expectedJourney = cannedJourney()
        val parsedJourney = Journey.fromJson(journeyJson)

        Assertions.assertEquals(expectedJourney, parsedJourney)
    }

}