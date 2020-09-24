package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MoveParserTest {

    @Test
    fun `Assert Move can be created from good json`() {
        val moveJson = """{"id":"M1", "date":"2021-02-28","status":"requested","created_at":"2020-09-07T15:30:48+01:00","updated_at":"2020-09-07T15:30:59+01:00","reference":"UKW4591N","move_type":"prison_transfer","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"PR1","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":"e05ee488-33a2-489f-9e57-5cab1871e2a0","rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"prison","to_location":"GNI","supplier":"geoamey"}
"""
        val moveDate = LocalDate.parse("2021-02-28", DateTimeFormatter.ISO_LOCAL_DATE)
        val expectedMove = Move(id = "M1", profileId = "PR1", reference = "UKW4591N", date = moveDate, status = "requested", fromLocation = "WYI", toLocation = "GNI")
        val parsedMode = Move.fromJson(moveJson)

        Assertions.assertEquals(expectedMove, parsedMode)
    }

}