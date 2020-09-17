package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import java.io.File
import java.time.LocalDate

object ReportingImporter {

    /**
     * Takes a list of files, and for each line in each file convert from JSON to entity of type T
     * @param files List of jsonl files to parse
     * @param f Lambda that takes a String and converts to entity of type T?
     * @return List of entities of type T
     */
    fun <T>read(files: List<File>, f: (j: String) -> T?): List<T>{
        return files.flatMap {
            it.readText().split("\n").map { json ->
                f(json)
            }
        }.filterNotNull()
    }
    fun importAsMoves(moveFiles: List<File>): Collection<Move> {
        val moves = read(moveFiles) { Move.fromJson(it) }
        return moves.map { it.id to it }.toMap().values
    }

    fun importAsMoveIdToJourneys(journeyFiles: List<File>): Map<String, List<Journey>> {
        val journeys = read(journeyFiles) {Journey.fromJson(it)}
        return journeys.map {it.id to it}.toMap().values.groupBy { it.moveId }
    }

    fun importAsEventableIdToEvents(eventFiles: List<File>): Map<String, List<Event>> {
        val events = read(eventFiles) {Event.fromJson(it)}
        return events.groupBy { it.eventableId }
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