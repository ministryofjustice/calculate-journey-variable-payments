package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PricesSpreadsheetGenerator
import java.io.File
import java.time.LocalDate

@Service
class SpreadsheetService(
  private val pricesSpreadsheetGenerator: PricesSpreadsheetGenerator,
  private val auditService: AuditService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun spreadsheet(authentication: Authentication, supplier: Supplier, startDate: LocalDate): File? {
    logger.info("Generating spreadsheet for supplier '$supplier', moves from '$startDate''")

    return pricesSpreadsheetGenerator.generate(supplier, startDate).also {
      auditService.create(AuditableEvent.downloadSpreadsheetEvent(startDate, supplier, authentication))
    }
  }
}
