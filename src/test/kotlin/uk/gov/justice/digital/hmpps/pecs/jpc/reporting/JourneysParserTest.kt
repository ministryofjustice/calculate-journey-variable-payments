package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Klaxon
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Move

class JourneysParserTest {

    @Test
    fun `Assert Journey can be created from json`() {
        val journeys = getReportLines("/reporting/journeys.jsonl")

        val expectedJourney = Journey(id="0036ae57-1eb7-4ffb-9a76-2c052b18c60b", moveId="d864dfcd-f41a-4030-972b-dd42ec268efa", billable=false, state="completed", supplier="geoamey", vehicleRegistration="UHE-92", fromLocation="GCS11", toLocation="HPS008")
        val parsedJourney = Journey.fromJson(journeys[0])

        Assertions.assertEquals(expectedJourney, parsedJourney)
    }

}