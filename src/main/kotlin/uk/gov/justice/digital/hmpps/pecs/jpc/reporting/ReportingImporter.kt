package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import java.io.File
import java.time.LocalDate

object ReportingImporter {

    fun importAsMoves(moveFiles: List<File>): Collection<Move> {
        val moves = moveFiles.flatMap {
            it.readText().split("\n").map { json ->
                Move.fromJson(json)
            }
        }
        return moves.filterNotNull().map { it.id to it }.toMap().values
    }

    fun importAsMoveIdToJourneys(journeyFiles: List<File>): Map<String, List<Journey>> {
        val journeys = journeyFiles.flatMap {
            it.readText().split("\n").map { json ->
                Journey.fromJson(json)
            }
        }
        return journeys.filterNotNull().map {it.id to it}.toMap().values.groupBy { it.moveId }
    }

    fun importAsEventableIdToEvents(eventFiles: List<File>): Map<String, List<Event>> {
        return eventFiles.flatMap {
            it.readText().split("\n").map { json ->
                Event.fromJson(json)
            }
        }.filterNotNull().groupBy { it.eventableId }
    }

    fun importAll(moveFiles: List<File>, journeyFiles: List<File>, eventFiles: List<File>): List<MoveWithJourneysAndEvents> {
        val moves = importAsMoves(moveFiles)
        val journeys = importAsMoveIdToJourneys(journeyFiles)
        val events = importAsEventableIdToEvents(eventFiles)

        val movesWithJourneysAndEvents = moves.map { move ->
            MoveWithJourneysAndEvents(
                    move = move,
                    journeysWithEvents = journeys.getValue(move.id).map { journey ->
                        JourneyWithEvents(journey = journey, events = events.getOrDefault(journey.id, listOf())) },
                    events = events.getOrDefault(move.id, listOf()
                    )
            )
        }
        return movesWithJourneysAndEvents
    }


    fun fileNamesForDate(entity: String, date: LocalDate): List<String> {
        return (1..date.dayOfMonth).map { day -> "${date.year}-${padZero(date.monthValue)}-${padZero(day)}-$entity.jsonl" }
    }

    private fun padZero(value: Int) = if (value < 10) "0${value}" else value.toString()

}