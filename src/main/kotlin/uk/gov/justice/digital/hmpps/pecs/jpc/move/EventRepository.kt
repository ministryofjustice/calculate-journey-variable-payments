package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Event

interface EventRepository : JpaRepository<Event, String> {

    fun findAllByEventableId(eventableId: String) : List<Event>

    fun findByEventableIdIn(eventableIds: List<String>) : List<Event>
}