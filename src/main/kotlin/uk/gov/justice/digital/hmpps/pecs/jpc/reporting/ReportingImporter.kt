package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import java.io.File
import java.time.LocalDate

object ReportingImporter {

    /**
     * Takes a list of files, and for each line in each file convert from JSON to entity of type T
     * @param files List of jsonl files to parse
     * @param f Lambda that takes a String and converts to entity of type T?
     * @return Sequence of entities of type T
     */
    fun <T>read(files: List<File>, f: (j: String) -> T?): Sequence<T>{
        return files.asSequence().flatMap {
            it.readText().split("\n").map { json ->
                f(json)
            }
        }.filterNotNull()
    }
    
    fun importAsProfileIdToPersonId(profileFiles: List<File>) : Map<String, String>{
        return read(profileFiles) {Profile.fromJson(it)}.
        associateBy(keySelector = {it.id}, valueTransform = {it.personId})
    }

    fun importAsPersonIdToPerson(peopleFiles: List<File>) : Map<String, Person>{
        return read(peopleFiles) {Person.fromJson(it)}.associateBy(Person::id)
    }
    
    fun importAsMoves(moveFiles: List<File>): Collection<Move> {
        return read(moveFiles) { Move.fromJson(it) }.associateBy(Move::id).values
    }

    fun importAsMoveIdToJourneys(journeyFiles: List<File>): Map<String, List<Journey>> {
        return read(journeyFiles) {Journey.fromJson(it)}.associateBy(Journey::id).values.groupBy(Journey::moveId)
    }

    fun importAsEventableIdToEvents(eventFiles: List<File>): Map<String, List<Event>> {
        return read(eventFiles) {Event.fromJson(it)}.groupBy(Event::eventableId)
    }

    fun importAll(moveFiles: List<File>, profileFiles: List<File>, peopleFiles: List<File>, journeyFiles: List<File>, eventFiles: List<File>): List<MovePersonJourneysEvents> {
        val moves = importAsMoves(moveFiles)
        val profileId2PersonId = importAsProfileIdToPersonId(profileFiles)
        val people = importAsPersonIdToPerson(peopleFiles)
        val journeys = importAsMoveIdToJourneys(journeyFiles)
        val events = importAsEventableIdToEvents(eventFiles)

        val movesWithJourneysAndEvents = moves.map { move ->
            MovePersonJourneysEvents(
                    move = move,
                    person = if(move.profileId == null) null else people[profileId2PersonId[move.profileId]],
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