package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

class ReportMoveParserTest {

  @Test
  fun `Assert Move can be created from JSON with null to_location, unknown status and unknown supplier`() {
    val moveJson =
      """{"id":"M1", "updated_at": "2020-06-16T10:20:30+01:00", "date":"2021-02-28","status":"WIBBLE","reference":"UKW4591N","move_type":"prison_transfer","cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"PR1","reason_comment":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"prison","to_location":null,"supplier":"WOBBLE"}
"""
    val parsedMove = Move.fromJson(moveJson)

    assertThat(parsedMove?.toNomisAgencyId).isNull()
    assertThat(parsedMove?.reference).isEqualTo("UKW4591N")
    assertThat(parsedMove?.supplier).isEqualTo(Supplier.UNKNOWN)
    assertThat(parsedMove?.status).isEqualTo(MoveStatus.unknown)
  }

  @Test
  fun `Correct JSON is created from Move and successfully parsed back to Move again`() {
    val move = reportMoveFactory()
    val moveJson = move.toJson()

    val expectedJson =
      """{"cancellation_reason" : "", "cancellation_reason_comment" : null, "from_location" : "WYI", "date" : "2021-02-28", "id" : "M1", "moveMonth" : 2, "moveYear" : 2021, "profile_id" : "PR1", "reference" : "UKW4591N", "from_location_type" : "prison", "to_location_type" : "court", "status" : "completed", "supplier" : "SERCO", "to_location" : "GNI", "updated_at" : "2020-06-16T10:20:30"}"""
    assertThat(moveJson).isEqualTo(expectedJson)

    val parsedMove = Move.fromJson(moveJson)
    assertThat(parsedMove).isEqualTo(move)
  }
}
