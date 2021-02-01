package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface AuditEventExtraRepository : CrudRepository<AuditEventExtra, UUID>
