package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.annotation.Import

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig


@ExtendWith(SpringExtension::class)
@Import(TestConfig::class)
@ActiveProfiles("test")
internal class ReportingParserTest {

    fun profileReports() : List<String> {
        val report1 = """{"id":"PR1","person_id":"PE1"}""".trimIndent()
        return listOf(report1)
    }

    fun personReports(): List<String> {
        val report1 = """
            {"id":"PE1","created_at":"2020-09-07T16:25:15+01:00","updated_at":"2020-09-07T16:25:24+01:00","criminal_records_office":null,"nomis_prison_number":null,"police_national_computer":"83SHX5/YL","prison_number":"PRISON1","latest_nomis_booking_id":null,"gender":"male","age":46}
        """.trimIndent()
        return listOf(report1)
    }

    fun moveReports() : List<String> {
        val report1 = """
            {"id":"M1","date":"2021-02-28","status":"requested","created_at":"2020-09-07T15:30:48+01:00","updated_at":"2020-09-07T15:30:59+01:00","reference":"A","move_type":"prison_transfer","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":null,"reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":"e05ee488-33a2-489f-9e57-5cab1871e2a0","rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"prison","to_location":"GNI","supplier":"geoamey"}
            {"id":"M2","date":"2020-09-07","status":"requested","created_at":"2020-09-07T15:44:08+01:00","updated_at":"2020-09-07T15:44:08+01:00","reference":"B","move_type":"court_appearance","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"d4168c75-21da-4694-866e-8fae71f327e6","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":null,"rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"court","to_location":"BATHYC","supplier":"geoamey"}
        """.trimIndent()

        // 2nd report file has the same first move, but now in approved status
        val report2 = """
            {"id":"M1","date":"2021-02-28","status":"completed","created_at":"2020-09-07T15:30:48+01:00","updated_at":"2020-09-07T15:30:59+01:00","reference":"C","move_type":"prison_transfer","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"PR1","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":"e05ee488-33a2-489f-9e57-5cab1871e2a0","rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"prison","to_location":"GNI","supplier":"geoamey"}
            {"id":"M3","date":"2020-09-07","status":"requested","created_at":"2020-09-07T15:44:08+01:00","updated_at":"2020-09-07T15:44:08+01:00","reference":"D","move_type":"court_appearance","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"d4168c75-21da-4694-866e-8fae71f327e6","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":null,"rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"court","to_location":"BATHYC","supplier":"geoamey"}
            {"id":"M4","date":"2020-09-07","status":"cancelled","created_at":"2020-09-07T15:44:08+01:00","updated_at":"2020-09-07T15:44:08+01:00","reference":"E","move_type":"court_appearance","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"d4168c75-21da-4694-866e-8fae71f327e6","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null,"allocation_id":null,"rejection_reason":null,"from_location_type":"prison","from_location":"WYI","to_location_type":"court","to_location":"BATHYC","supplier":"geoamey"}
        """.trimIndent()

        return listOf(report1, report2)
    }


    fun eventReports() : List<String> {
        val report1 = """
            {"id": "E1", "type": "MoveCancel", "actioned_by": "serco", "notes": "", "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-08-03 15:43:12 +0100", "eventable_id": "M1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"cancellation_reason": "made_in_error", "cancellation_reason_comment": "cancelled because the prisoner refused to move"}}
            {"id": "E2", "type": "MoveRedirect", "actioned_by": "geoamey", "notes": null, "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-08-03 15:43:12 +0100", "eventable_id": "M2", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"to_location": "BRSTCC"}}
        """.trimIndent()

        val report2 = """
            {"id": "E3", "type": "JourneyCancel", "actioned_by": "serco", "notes": "", "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-08-03 15:43:12 +0100", "eventable_id": "J1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"cancellation_reason": "made_in_error", "cancellation_reason_comment": "cancelled because the prisoner refused to move"}}
            {"id": "E4", "type": "MoveRedirect", "actioned_by": "geoamey", "notes": null, "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-08-03 15:43:12 +0100", "eventable_id": "M1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"to_location": "BRSTCC"}}
            {"id": "E5", "type": "MoveUnknownEvent", "actioned_by": "geoamey", "notes": null, "created_at": "2020-08-03 15:43:12 +0100", "updated_at": "2020-08-03 15:43:12 +0100", "eventable_id": "M1", "eventable_type": "move", "occurred_at": "2020-06-16T10:20:30+01:00", "recorded_at": "2020-06-16T10:20:30+01:00", "details": {"to_location": "BRSTCC"}}
        """.trimIndent()

        return listOf(report1, report2)
    }

    fun journeyReports() : List<String>{
        val report1 = """
        {"id":"J1","move_id":"M1","billable":false,"state":"requested","client_timestamp":"2020-09-08T12:49:00+01:00","created_at":"2020-09-07T15:00:58+01:00","updated_at":"2020-09-07T15:00:58+01:00","from_location_type":"probation_office","from_location":"GCS11","to_location_type":"probation_office","to_location":"HPS008","supplier":"geoamey","vehicle_registration":"UHE-92"}
        {"id":"J2","move_id":"M2","billable":false,"state":"completed","client_timestamp":"2020-09-08T06:55:00+01:00","created_at":"2020-09-07T15:01:01+01:00","updated_at":"2020-09-07T15:01:01+01:00","from_location_type":"court","from_location":"PRSTYC","to_location_type":"probation_office","to_location":"MRS023","supplier":"serco","vehicle_registration":"BNX-76"}
    """.trimIndent()

        // First journey has same id, Second journey has the same move id, 3rd journey is for a different move
        val report2 = """
        {"id":"J1","move_id":"M1","billable":true,"state":"completed","client_timestamp":"2020-09-08T12:49:00+01:00","created_at":"2020-09-07T15:00:58+01:00","updated_at":"2020-09-07T15:00:58+01:00","from_location_type":"probation_office","from_location":"GCS11","to_location_type":"probation_office","to_location":"HPS008","supplier":"geoamey","vehicle_registration":"UHE-92"}
        {"id":"J3","move_id":"M2","billable":false,"state":"completed","client_timestamp":"2020-09-08T06:55:00+01:00","created_at":"2020-09-07T15:01:01+01:00","updated_at":"2020-09-07T15:01:01+01:00","from_location_type":"court","from_location":"PRSTYC","to_location_type":"probation_office","to_location":"MRS023","supplier":"serco","vehicle_registration":"BNX-76"}
        {"id":"J4","move_id":"M3","billable":false,"state":"cancelled","client_timestamp":"2020-09-08T06:55:00+01:00","created_at":"2020-09-07T15:01:01+01:00","updated_at":"2020-09-07T15:01:01+01:00","from_location_type":"court","from_location":"PRSTYC","to_location_type":"probation_office","to_location":"MRS023","supplier":"serco","vehicle_registration":"BNX-76"}
        {"id":"J5","move_id":"M4","billable":false,"state":"proposed","client_timestamp":"2020-09-08T06:55:00+01:00","created_at":"2020-09-07T15:01:01+01:00","updated_at":"2020-09-07T15:01:01+01:00","from_location_type":"court","from_location":"PRSTYC","to_location_type":"probation_office","to_location":"MRS023","supplier":"serco","vehicle_registration":"BNX-76"}
    """.trimIndent()

        return listOf(report1, report2)
    }

    @Test
    fun `Get import moves should return only unique, completed or cancelled moves`() {
        val moves = ReportingParser.parseAsMoves(moveReports())
        //There should be 2 moves because the first one in both files is the same
        Assertions.assertEquals(2, moves.size)

        // M1 should be complete, M4 cancelled
        Assertions.assertEquals(MoveStatus.COMPLETED.value, moves.find{it.id == "M1"}?.status)
        Assertions.assertEquals(MoveStatus.CANCELLED.value, moves.find{it.id == "M4"}?.status)

    }

    @Test
    fun `Assert no Move created from bad json and no exception generated`() {
        val moveJsonWithNullFromLocation = """{"id":"M1", "date":"2021-02-28","status":"requested","reference":"UKW4591N","move_type":"prison_transfer","additional_information":null,"time_due":null,"cancellation_reason":null,"cancellation_reason_comment":null,"profile_id":"PR1","reason_comment":null,"move_agreed":null,"move_agreed_by":null,"date_from":null,"date_to":null, "rejection_reason":null,"from_location_type":"prison","from_location":null,"to_location_type":"prison","to_location":"GNI","supplier":"geoamey"}
"""
        val moves = ReportingParser.parseAsMoves(listOf(moveJsonWithNullFromLocation))

        Assertions.assertEquals(0, moves.size)
    }

    @Test
    fun `Get import journeys`() {

        val journeys = ReportingParser.parseAsMoveIdToJourneys(journeyReports())

        // Journeys should be grouped by the 3 unique move ids (with non completed/cancelled filtered)
        Assertions.assertEquals(setOf("M1", "M2", "M3"), journeys.keys)

        // Move 1 should have 1 journey (the same updated journey)
        Assertions.assertEquals(1, journeys.getValue("M1").size)

        // This journey should have been updated to billable=true
        Assertions.assertEquals(true, journeys.getValue("M1")[0].billable)

        // Move 2 should have 2 journeys
        Assertions.assertEquals(2, journeys.getValue("M2").size)
    }

    @Test
    fun `Get import events`() {

        val events = ReportingParser.parseAsEventableIdToEvents(eventReports())

        // There should be 3 unique eventable Ids
        Assertions.assertEquals(3, events.size)

        // Eventable 1 should have 2 events
        Assertions.assertEquals(2, events.getValue("M1").size)

        // Eventable 2 should have 1 event
        Assertions.assertEquals(1, events.getValue("M2").size)
    }

    @Test
    fun `import all`() {
        val movesWithJourneysAndEvents = ReportingParser.parseAll(
                moveFiles = moveReports(),
                peopleFiles = personReports(),
                profileFiles = profileReports(),
                journeyFiles = journeyReports(),
                eventFiles = eventReports()
        )

        // There should be 2 distinct moves from the 5 in the files
        Assertions.assertEquals(2, movesWithJourneysAndEvents.size)

        val move1 = movesWithJourneysAndEvents.find { it.move.id == "M1" }!!

        // Move1 should have two events
        Assertions.assertEquals(listOf("E1", "E4"), move1.events.map{it.id})

        // Move1's first journey should have event 3
        Assertions.assertEquals(listOf("E3"), move1.journeysWithEvents[0].events.map { it.id })

        // Move 1 should have Person PE1
        Assertions.assertEquals("PE1", move1.person?.id)
    }
}

