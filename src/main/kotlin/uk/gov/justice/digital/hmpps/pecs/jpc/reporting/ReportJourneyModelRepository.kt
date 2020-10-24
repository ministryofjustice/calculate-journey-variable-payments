package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface ReportJourneyModelRepository : CrudRepository<ReportJourneyModel, UUID> {

}