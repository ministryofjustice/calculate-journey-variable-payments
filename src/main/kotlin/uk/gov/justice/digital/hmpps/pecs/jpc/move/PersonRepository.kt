package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Person

interface PersonRepository : CrudRepository<Person, String> {

}