package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Event

interface EventRepository : CrudRepository<Event, String> {
    fun findAllByEventableId(eventableId: String) : List<Event>

}