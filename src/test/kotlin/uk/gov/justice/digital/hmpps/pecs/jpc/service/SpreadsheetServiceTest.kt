package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PricesSpreadsheetGenerator
import java.time.LocalDate

internal class SpreadsheetServiceTest {

  private val authentication: Authentication = mock()

  private val pricesSpreadsheetGenerator: PricesSpreadsheetGenerator = mock()

  private val auditService: AuditService = mock()

  private val service: SpreadsheetService = SpreadsheetService(pricesSpreadsheetGenerator, auditService)

  @Test
  internal fun `service interactions for Serco`() {
    verifyInteractionsFor(Supplier.SERCO)
  }

  @Test
  internal fun `service interactions for GEOAmey`() {
    verifyInteractionsFor(Supplier.GEOAMEY)
  }

  private fun verifyInteractionsFor(supplier: Supplier) {
    service.spreadsheet(authentication, supplier, LocalDate.of(2021, 2, 22))

    verify(pricesSpreadsheetGenerator).generate(supplier, LocalDate.of(2021, 2, 22))
    verify(auditService).create(
      AuditableEvent.downloadSpreadsheetEvent(
        LocalDate.of(2021, 2, 22),
        supplier,
        authentication
      )
    )
  }
}
