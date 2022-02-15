package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PricesSpreadsheetGenerator
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.io.File
import java.time.LocalDate

private val logger = loggerFor<JourneyPriceCatalogueService>()

@Transactional
@Service
class JourneyPriceCatalogueService(
  private val pricesSpreadsheetGenerator: PricesSpreadsheetGenerator,
  private val auditService: AuditService
) {

  fun generate(authentication: Authentication, supplier: Supplier, startDate: LocalDate): File? {
    logger.info("Generating spreadsheet for supplier '$supplier', moves from '$startDate''")

    return Result.runCatching {
      pricesSpreadsheetGenerator.generate(supplier, startDate).also {
        auditService.create(AuditableEvent.downloadSpreadsheetEvent(startDate, supplier, authentication))
      }
    }.getOrElse { exception ->
      auditService.create(AuditableEvent.downloadSpreadsheetFailure(startDate, supplier, authentication))
      throw exception
    }
  }
}
