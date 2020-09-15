package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun cannedJourney(): Journey{
    val clientTimestamp = LocalDateTime.parse("2020-09-08T12:49:00+01:00", DateTimeFormatter.ISO_DATE_TIME)
    val journey = Journey(id= UUID.fromString("0036ae57-1eb7-4ffb-9a76-2c052b18c60b"), moveId=UUID.fromString("d864dfcd-f41a-4030-972b-dd42ec268efa"), clientTimestamp = clientTimestamp, billable=false, state="completed", supplier="geoamey", vehicleRegistration="UHE-92", fromLocation="GCS11", toLocation="HPS008")
    return journey
}
class JourneysParserTest {

    @Test
    fun `Assert Journey can be created from json`() {
        val journeyJson = """{"id":"0036ae57-1eb7-4ffb-9a76-2c052b18c60b","move_id":"d864dfcd-f41a-4030-972b-dd42ec268efa","billable":false,"state":"completed","client_timestamp":"2020-09-08T12:49:00+01:00","created_at":"2020-09-07T15:00:58+01:00","updated_at":"2020-09-07T15:00:58+01:00","from_location_type":"probation_office","from_location":"GCS11","to_location_type":"probation_office","to_location":"HPS008","supplier":"geoamey","vehicle_registration":"UHE-92"}
"""
        val expectedJourney = cannedJourney()
        val parsedJourney = Journey.fromJson(journeyJson)

        Assertions.assertEquals(expectedJourney, parsedJourney)
    }

}