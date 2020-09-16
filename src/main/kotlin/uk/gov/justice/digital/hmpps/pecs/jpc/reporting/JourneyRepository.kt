package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface JourneyRepository : CrudRepository<Journey, UUID> {

}