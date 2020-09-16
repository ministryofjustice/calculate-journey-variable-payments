package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EventRepository : CrudRepository<Event, UUID> {

    @Query("SELECT e FROM Event e WHERE  e.eventableId IN (:eventableIds)")
    fun findByEventableIds(
            @Param("eventableIds") eventableIds: List<UUID>
    ): List<Event>

}