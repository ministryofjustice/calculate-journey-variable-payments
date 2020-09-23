package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import java.time.LocalDate

object ReportingParser {

    /**
     * Takes a list of files content, and for each line in each file convert from JSON to entity of type T
     * @param file List of jsonl files content to parse
     * @param f Lambda that takes a String and converts to entity of type T?
     * @return Sequence of entities of type T
     */
    fun <T>read(files: List<String>, f: (j: String) -> T?): Sequence<T>{
        return files.asSequence().flatMap {
            it.split("\n").map { json ->
                f(json)
            }
        }.filterNotNull()
    }
    
    fun parseAsProfileIdToPersonId(profileFiles: List<String>) : Map<String, String>{
        return read(profileFiles) {Profile.fromJson(it)}.
        associateBy(keySelector = {it.id}, valueTransform = {it.personId})
    }

    fun parseAsPersonIdToPerson(peopleFiles: List<String>) : Map<String, Person>{
        return read(peopleFiles) {Person.fromJson(it)}.associateBy(Person::id)
    }
    
    fun parseAsMoves(moveFiles: List<String>): Collection<Move> {
        return read(moveFiles) { Move.fromJson(it) }.associateBy(Move::id).values
    }


    fun parseAsMoveIdToJourneys(journeyFiles: List<String>): Map<String, List<Journey>> {
        return read(journeyFiles) {Journey.fromJson(it)}.associateBy(Journey::id).values.groupBy(Journey::moveId)
    }

    fun parseAsEventableIdToEvents(eventFiles: List<String>): Map<String, List<Event>> {
        return read(eventFiles) {Event.fromJson(it)}.groupBy(Event::eventableId)
    }

    fun parseAll(moveFiles: List<String>, profileFiles: List<String>, peopleFiles: List<String>, journeyFiles: List<String>, eventFiles: List<String>): List<MovePersonJourneysEvents> {
        val moves = parseAsMoves(moveFiles)
        val profileId2PersonId = parseAsProfileIdToPersonId(profileFiles)
        val people = parseAsPersonIdToPerson(peopleFiles)
        val journeys = parseAsMoveIdToJourneys(journeyFiles)
        val events = parseAsEventableIdToEvents(eventFiles)

        val movesWithJourneysAndEvents = moves.map { move ->
            MovePersonJourneysEvents(
                    move = move,
                    person = if(move.profileId == null) null else people[profileId2PersonId[move.profileId]],
                    journeysWithEvents = journeys.getOrDefault(move.id, listOf()).map { journey ->
                        JourneyWithEvents(journey = journey, events = events.getOrDefault(journey.id, listOf())) },
                    events = events.getOrDefault(move.id, listOf()
                    )
            )
        }
        return movesWithJourneysAndEvents
    }
}