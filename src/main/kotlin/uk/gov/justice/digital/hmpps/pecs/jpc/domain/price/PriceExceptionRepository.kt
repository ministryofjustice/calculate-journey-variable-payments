package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PriceExceptionRepository : JpaRepository<PriceException, UUID>
