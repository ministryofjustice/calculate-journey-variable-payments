package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MoveParserTest {

    @Test
    fun `Assert Move can be created from good json with null to location`() {
        val fromPrison = fromPrisonNomisAgencyId()
        val toCourt = toCourtNomisAgencyId()

        val moveJson = """{"id":"M1", "date":"2021-02-28","status":"requested","reference":"UKW4591N","move_type":"prison_transfer","cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"PR1","reason_comment":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"prison","to_location":null,"supplier":"geoamey"}
"""
        val moveDate = LocalDate.parse("2021-02-28", DateTimeFormatter.ISO_LOCAL_DATE)
        val expectedMove = Move(id = "M1", supplier = "geoamey", profileId = "PR1", reference = "UKW4591N", date = moveDate, status = "requested", fromLocation = fromPrison, fromLocationType = "prison", toLocation = toCourt, toLocationType = "court")


        val parsedMove = Move.fromJson(moveJson)

        assertThat(parsedMove?.toLocation).isNull()
        assertThat(parsedMove?.reference).isEqualTo("UKW4591N")

    }

    @Test
    fun `Move json created from Move, and successfully parsed back to Move again`(){
        val move = moveFactory()

        val moveJson = move.toJson()

        val expectedJson = """{"cancellation_reason" : "", "cancellation_reason_comment" : null, "date" : "2021-02-28", "from_location" : "WYI", "from_location_type" : "prison", "id" : "M1", "profile_id" : "PR1", "reference" : "UKW4591N", "status" : "completed", "supplier" : "serco", "to_location" : "GNI", "to_location_type" : "court"}"""
        assertThat(moveJson).isEqualTo(expectedJson)

        val parsedMove = Move.fromJson(moveJson)
        assertThat(parsedMove).isEqualTo(move)
    }

}