package uk.gov.justice.digital.hmpps.pecs.jpc.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.ReportMove
import uk.gov.justice.digital.hmpps.pecs.jpc.report.reportMoveFactory


class ReportMoveParserTest {

    @Test
    fun `Assert Move can be created from good json with null to location`() {

        val moveJson = """{"id":"M1", "date":"2021-02-28","status":"requested","reference":"UKW4591N","move_type":"prison_transfer","cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"PR1","reason_comment":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"prison","to_location":null,"supplier":"geoamey"}
"""
        val parsedMove = ReportMove.fromJson(moveJson)

        assertThat(parsedMove?.toNomisAgencyId).isNull()
        assertThat(parsedMove?.reference).isEqualTo("UKW4591N")

    }

    @Test
    fun `Move json created from Move, and successfully parsed back to Move again`(){
        val move = reportMoveFactory()
        val moveJson = move.toJson()

        val expectedJson = """{"cancellation_reason" : "", "cancellation_reason_comment" : null, "from_location_type" : "prison", "from_location" : "WYI", "id" : "M1", "date" : "2021-02-28", "profile_id" : "PR1", "reference" : "UKW4591N", "status" : "completed", "supplier" : "serco", "to_location_type" : "court", "to_location" : "GNI"}"""
        assertThat(moveJson).isEqualTo(expectedJson)

        val parsedMove = ReportMove.fromJson(moveJson)
        assertThat(parsedMove).isEqualTo(move)
    }

}