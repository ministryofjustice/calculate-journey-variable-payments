package uk.gov.justice.digital.hmpps.pecs.jpc.importer.report

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.equalsStringCaseInsensitive

@Component
object ReportParser {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Takes a list of files content, and for each line in each file convert from JSON to entity of type T
     * @param file List of jsonl files content to parse
     * @param f Lambda that takes a String and converts to entity of type T?
     * @return Sequence of entities of type T
     */
    fun <T> read(files: List<String>, f: (j: String) -> T?): Sequence<T> {
        return files.asSequence().flatMap {
            it.split("\n").filter { it.isNotEmpty() }.map { json ->
                try {
                    f(json)
                } catch (e: Exception) {
                    // Simply warn and go on to the next one
                    logger.warn("ERROR parsing json $json: ${e.message}")
                    null
                }
            }
        }.filterNotNull()
    }

    fun parseAsProfileIdToPersonId(profileFiles: List<String>): Map<String, String> {
        logger.info("Parsing profiles")
        return read(profileFiles) { ReportProfile.fromJson(it) }.associateBy(keySelector = { it.id }, valueTransform = { it.personId })
    }

    fun parseAsPersonIdToPerson(peopleFiles: List<String>): Map<String, ReportPerson> {
        logger.info("Parsing people")
        return read(peopleFiles) { ReportPerson.fromJson(it) }.associateBy(ReportPerson::id)
    }

    fun parseAsMoves(supplier: Supplier, moveFiles: List<String>): Collection<ReportMove> {
        logger.info("Parsing moves")
        return read(moveFiles) { ReportMove.fromJson(it) }.
        filter {
            supplier.equalsStringCaseInsensitive(it.supplier) &&
            (MoveStatus.COMPLETED.equalsStringCaseInsensitive(it.status) || MoveStatus.CANCELLED.equalsStringCaseInsensitive(it.status))
        }.
        associateBy(ReportMove::id).values // associateBy will only include latest Move by id
    }


    fun parseAsMoveIdToJourneys(journeyFiles: List<String>): Map<String, List<ReportJourney>> {
        logger.info("Parsing journeys")
        return read(journeyFiles) { ReportJourney.fromJson(it) }.
        filter {  (JourneyState.COMPLETED.equalsStringCaseInsensitive(it.state) || JourneyState.CANCELLED.equalsStringCaseInsensitive(it.state)) }.
        associateBy(ReportJourney::id).values.groupBy(ReportJourney::moveId) // associateBy will only include latest Journey by id
    }

    fun parseAsEventableIdToEvents(eventFiles: List<String>): Map<String, List<ReportEvent>> {
        logger.info("Parsing events")
        return read(eventFiles) { ReportEvent.fromJson(it) }.
        filter { EventType.types.contains(it.type) }.
        distinctBy { it.id }. // filter duplicates (shouldn't be any, but just in case)
        groupBy(ReportEvent::eventableId)
    }

    fun parseAll(supplier: Supplier, moveFiles: List<String>, profileFiles: List<String>, peopleFiles: List<String>, journeyFiles: List<String>, eventFiles: List<String>): List<Report> {
        val moves = parseAsMoves(supplier, moveFiles)
        val profileId2PersonId = parseAsProfileIdToPersonId(profileFiles)
        val people = parseAsPersonIdToPerson(peopleFiles)
        val journeys = parseAsMoveIdToJourneys(journeyFiles)
        val events = parseAsEventableIdToEvents(eventFiles)

        return moves.map { move ->
            Report(
                    reportMove = move,
                    reportPerson = move.profileId?.let {people[profileId2PersonId[it]]},
                    journeysWithEventReports = journeys.getOrDefault(move.id, listOf()).map { journey ->
                        ReportJourneyWithEvents(reportJourney = journey, reportEvents = events.getOrDefault(journey.id, listOf()))
                    },
                    reportEvents = events.getOrDefault(move.id, listOf()
                    )
            )
        }
    }

}