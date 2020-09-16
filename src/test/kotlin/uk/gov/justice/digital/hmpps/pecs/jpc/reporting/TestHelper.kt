package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

val occurredAndRecordedAt = LocalDateTime.parse("2020-06-16T10:20:30+01:00", DateTimeFormatter.ISO_DATE_TIME)
val supplier = "serco"

fun getReportLines(path: String): List<String> {
    return object {}.javaClass.getResource(path).readText().split("\n")
}

fun cannedMove(): Move {
    val moveDate = LocalDate.parse("2021-02-28", DateTimeFormatter.ISO_LOCAL_DATE)
    val move = Move(
            id = UUID.fromString("02b4c0f5-4d85-4fb6-be6c-53d74b85bf2e"),
            reference = "UKW4591N",
            date = moveDate,
            status = "requested",
            fromLocation = "WYI",
            toLocation = "GNI")
    return move
}

fun cannedMoveEvent(): Event{
    val event = Event(
            id=UUID.fromString("00699c3d-b4db-4e9d-858c-36f81aa19815"),
            type="MoveCancel",
            actionedBy= supplier,
            eventableType="move",
            eventableId= cannedMove().id,
            details= mapOf("cancellation_reason" to "made_in_error", "cancellation_reason_comment" to "cancelled because the prisoner refused to move"),
            occurredAt=occurredAndRecordedAt,
            recordedAt=occurredAndRecordedAt,
            notes="")
    return event
}
fun cannedJourney(): Journey{
    val journey = Journey(
            id= UUID.fromString("0036ae57-1eb7-4ffb-9a76-2c052b18c60b"),
            moveId= cannedMove().id,
            clientTimestamp = occurredAndRecordedAt,
            billable=false, state="completed",
            supplier= supplier,
            vehicleRegistration="UHE-92",
            fromLocation="GCS11",
            toLocation="HPS008")
    return journey
}

fun cannedJourneyEvent(): Event{
    val event = Event(
            id= UUID.fromString("00699c3d-b4db-4e9d-858c-36f81aa19818"),
            type="JourneyStart",
            actionedBy= supplier,
            eventableType="journey",
            eventableId= cannedJourney().id,
            details= mapOf(),
            occurredAt=occurredAndRecordedAt,
            recordedAt=occurredAndRecordedAt,
            notes="")
    return event
}





