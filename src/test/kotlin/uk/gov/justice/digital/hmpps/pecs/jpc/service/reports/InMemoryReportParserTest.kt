package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus

internal class InMemoryReportParserTest {

  private fun moveReports(): List<String> {
    val report1 =
      """
        {"id":"M1","date":"2021-02-28","status":"requested","created_at":"2020-09-07T15:30:48+01:00","updated_at":"2020-09-07T15:30:59+01:00","reference":"A","move_type":"prison_transfer","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":null,"reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":"e05ee488-33a2-489f-9e57-5cab1871e2a0","rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"prison","to_location":"GNI","supplier":"GEOAMEY"}
        {"id":"M2","date":"2020-09-07","status":"requested","created_at":"2020-09-07T15:44:08+01:00","updated_at":"2020-09-07T15:44:08+01:00","reference":"B","move_type":"court_appearance","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"d4168c75-21da-4694-866e-8fae71f327e6","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":null,"rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"court","to_location":"BATHYC","supplier":"GEOAMEY"}
      """.trimIndent()

    // 2nd move file has the same first move, but now in approved status
    val report2 =
      """
        {"id":"M1","date":"2021-02-28","status":"completed","created_at":"2020-09-07T15:30:48+01:00","updated_at":"2020-09-07T15:30:59+01:00","reference":"C","move_type":"prison_transfer","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"PR1","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":"e05ee488-33a2-489f-9e57-5cab1871e2a0","rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"prison","to_location":"GNI","supplier":"GEOAMEY"}
        {"id":"M3","date":"2020-09-07","status":"requested","created_at":"2020-09-07T15:44:08+01:00","updated_at":"2020-09-07T15:44:08+01:00","reference":"D","move_type":"court_appearance","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"d4168c75-21da-4694-866e-8fae71f327e6","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":null,"rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"court","to_location":"BATHYC","supplier":"GEOAMEY"}
        {"id":"M4","date":"2020-09-07","status":"cancelled","created_at":"2020-09-07T15:44:08+01:00","updated_at":"2020-09-07T15:44:08+01:00","reference":"E","move_type":"court_appearance","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"d4168c75-21da-4694-866e-8fae71f327e6","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":null,"rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"court","to_location":"BATHYC","supplier":"GEOAMEY"}
        {"id":"M5","date":"2021-02-28","status":"completed","created_at":"2020-09-07T15:30:48+01:00","updated_at":"2020-09-07T15:30:59+01:00","reference":"F","move_type":"prison_transfer","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"PR1","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":"e05ee488-33a2-489f-9e57-5cab1871e2a0","rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"prison","to_location":"GNI","supplier":"SERCO"}
      """.trimIndent()

    return listOf(report1, report2)
  }

  private fun eventReports(): List<String> {
    val report1 =
      """
        {"id": "E1", "type": "MoveCancel", "supplier": "SERCO", "notes": "", "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-09-07T15:30:59+01:00", "eventable_id": "M1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"cancellation_reason": "made_in_error", "cancellation_reason_comment": "cancelled because the prisoner refused to move"}}
        {"id": "E2", "type": "MoveRedirect", "supplier": "GEOAMEY", "notes": null, "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-09-07T15:30:59+01:00", "eventable_id": "M2", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"to_location": "BRSTCC"}}
      """.trimIndent()

    val report2 =
      """
        {"id": "E3", "type": "JourneyCancel", "supplier": "SERCO", "notes": "", "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-09-07T15:30:59+01:00", "eventable_id": "J1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"cancellation_reason": "made_in_error", "cancellation_reason_comment": "cancelled because the prisoner refused to move"}}
        {"id": "E4", "type": "MoveRedirect", "supplier": "GEOAMEY", "notes": null, "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-09-07T15:30:59+01:00", "eventable_id": "M1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"to_location": "BRSTCC"}}
        {"id": "E5", "type": "MoveUnknownEvent", "supplier": "GEOAMEY", "notes": null, "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-09-07T15:30:59+01:00", "eventable_id": "M1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"to_location": "BRSTCC"}}
      """.trimIndent()

    return listOf(report1, report2)
  }

  private fun journeyReports(): List<String> {
    val report1 =
      """
        {"id":"J1","move_id":"M1","billable":false,"state":"requested","client_timestamp":"2020-09-08T12:49:00+01:00","created_at":"2020-09-07T15:00:58+01:00","updated_at":"2020-09-07T15:00:58+01:00","from_location_type":"probation_office","from_location":"GCS11","to_location_type":"probation_office","to_location":"HPS008","supplier":"GEOAMEY","vehicle_registration":"UHE-92"}
        {"id":"J2","move_id":"M2","billable":false,"state":"completed","client_timestamp":"2020-09-08T06:55:00+01:00","created_at":"2020-09-07T15:01:01+01:00","updated_at":"2020-09-07T15:01:01+01:00","from_location_type":"court","from_location":"PRSTYC","to_location_type":"probation_office","to_location":"MRS023","supplier":"SERCO","vehicle_registration":"BNX-76"}
      """.trimIndent()

    // First journey has same id, Second journey has the same move id, 3rd journey is for a different move
    val report2 =
      """
        {"id":"J1","move_id":"M1","billable":true,"state":"completed","client_timestamp":"2020-09-08T12:49:00+01:00","created_at":"2020-09-07T15:00:58+01:00","updated_at":"2020-09-07T15:00:58+01:00","from_location_type":"probation_office","from_location":"GCS11","to_location_type":"probation_office","to_location":"HPS008","supplier":"GEOAMEY","vehicle_registration":"UHE-92"}
        {"id":"J3","move_id":"M2","billable":false,"state":"completed","client_timestamp":"2020-09-08T06:55:00+01:00","created_at":"2020-09-07T15:01:01+01:00","updated_at":"2020-09-07T15:01:01+01:00","from_location_type":"court","from_location":"PRSTYC","to_location_type":"probation_office","to_location":"MRS023","supplier":"SERCO","vehicle_registration":"BNX-76"}
        {"id":"J4","move_id":"M3","billable":false,"state":"cancelled","client_timestamp":"2020-09-08T06:55:00+01:00","created_at":"2020-09-07T15:01:01+01:00","updated_at":"2020-09-07T15:01:01+01:00","from_location_type":"court","from_location":"PRSTYC","to_location_type":"probation_office","to_location":"MRS023","supplier":"SERCO","vehicle_registration":"BNX-76"}
        {"id":"J5","move_id":"M4","billable":false,"state":"proposed","client_timestamp":"2020-09-08T06:55:00+01:00","created_at":"2020-09-07T15:01:01+01:00","updated_at":"2020-09-07T15:01:01+01:00","from_location_type":"court","from_location":"PRSTYC","to_location_type":"probation_office","to_location":"MRS023","supplier":"SERCO","vehicle_registration":"BNX-76"}
      """.trimIndent()

    return listOf(report1, report2)
  }

  @Test
  fun `Get import moves should return all moves`() {

    val moves = InMemoryReportParser.parseAsMoves(moveReports())
    assertThat(moves.map { it.moveId }).containsExactly("M1", "M2", "M3", "M4", "M5")

    // M1 should be complete
    assertThat(MoveStatus.completed).isEqualTo(moves.find { it.moveId == "M1" }?.status)
  }

  @Test
  fun `no move created from bad JSON and no exception generated`() {
    val moveJsonWithNullFromLocation =
      """
        {"id":"M1", "date":"2021-02-28","status":"requested","reference":"UKW4591N","move_type":"prison_transfer","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"PR1","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null, "rejection_reason":null,"from_location_type":"prison","from_location":null,"to_location_type":"prison","to_location":"GNI","supplier":"GEOAMEY"}
      """

    assertThat(InMemoryReportParser.parseAsMoves(listOf(moveJsonWithNullFromLocation))).hasSize(0)
  }

  @Test
  fun `parse move ids to journeys`() {

    val journeys = InMemoryReportParser.parseAsMoveIdToJourneys(journeyReports())

    // Journeys should be grouped by the 3 unique move ids (with non completed/cancelled filtered)
    assertThat(journeys.keys).containsExactlyInAnyOrder("M1", "M2", "M3")

    // Move 1 should have 1 journey (the same updated journey)
    assertThat(journeys["M1"]).hasSize(1)

    // This journey should have been updated to billable=true
    assertThat(journeys["M1"]?.first()?.billable).isTrue

    // Move 2 should have 2 journeys
    assertThat(journeys["M2"]).hasSize(2)
  }

  @Test
  fun `parse event reports only`() {
    val events = InMemoryReportParser.parseAsEventableIdToEvents(eventReports())

    // There should be 3 unique eventable Ids
    assertThat(events).hasSize(3)

    // Eventable 1 should have 2 events
    assertThat(events["M1"]).hasSize(2)

    // Eventable 2 should have 1 event
    assertThat(events["M2"]).hasSize(1)
  }

  @Test
  fun `parse move, journey and event and event reports`() {
    val movesWithJourneysAndEvents = InMemoryReportParser.parseMovesJourneysEvents(
      moveFiles = moveReports(),
      journeyFiles = journeyReports(),
      eventFiles = eventReports()
    )

    val move1 = movesWithJourneysAndEvents.find { it.moveId == "M1" }!!

    // Move1 should have two events
    assertThat(move1.events.map { it.eventId }).containsExactly("E1", "E4")

    // Move1's first journey should have event 3
    assertThat(move1.journeys.first().events?.map { it.eventId }).containsExactly("E3")
  }
}
