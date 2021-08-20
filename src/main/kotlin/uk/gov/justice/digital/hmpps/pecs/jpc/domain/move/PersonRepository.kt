package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.Person

interface PersonRepository : JpaRepository<Person, String>
