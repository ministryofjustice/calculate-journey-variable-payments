package uk.gov.justice.digital.hmpps.pecs.jpc.domain.event

import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository : JpaRepository<Event, String> {

  fun findAllByEventableId(eventableId: String): List<Event>

  fun findAllByEventableIdIn(eventableIds: List<String>): List<Event>

  fun findByEventableIdIn(eventableIds: List<String>): List<Event>
}
