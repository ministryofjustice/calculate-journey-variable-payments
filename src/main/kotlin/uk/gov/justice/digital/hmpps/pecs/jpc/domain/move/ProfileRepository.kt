package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.Profile

interface ProfileRepository : JpaRepository<Profile, String>