package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Profile

interface ProfileRepository : JpaRepository<Profile, String> {

}