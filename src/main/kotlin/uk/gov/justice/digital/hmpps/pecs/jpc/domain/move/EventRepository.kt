package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.Event

interface EventRepository : JpaRepository<Event, String> {

  fun findAllByEventableId(eventableId: String): List<Event>

  fun findAllByEventableIdIn(eventableIds: List<String>): List<Event>

  fun findByEventableIdIn(eventableIds: List<String>): List<Event>
}
