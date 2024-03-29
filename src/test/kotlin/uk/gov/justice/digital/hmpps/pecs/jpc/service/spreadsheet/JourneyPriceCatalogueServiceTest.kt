package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import java.time.LocalDate

internal class JourneyPriceCatalogueServiceTest {

  private val authentication: Authentication = mock()

  private val pricesSpreadsheetGenerator: PricesSpreadsheetGenerator = mock()

  private val auditService: AuditService = mock()

  private val service: JourneyPriceCatalogueService = JourneyPriceCatalogueService(pricesSpreadsheetGenerator, auditService)

  @Test
  internal fun `service interactions for Serco`() {
    verifyInteractionsFor(Supplier.SERCO)
  }

  @Test
  internal fun `service interactions for GEOAmey`() {
    verifyInteractionsFor(Supplier.GEOAMEY)
  }

  private fun verifyInteractionsFor(supplier: Supplier) {
    service.generate(authentication, supplier, LocalDate.of(2021, 2, 22))

    verify(pricesSpreadsheetGenerator).generate(supplier, LocalDate.of(2021, 2, 22))
    verify(auditService).create(
      AuditableEvent.downloadSpreadsheetEvent(
        LocalDate.of(2021, 2, 22),
        supplier,
        authentication,
      ),
    )
  }

  @Test
  internal fun `failure to download Serco spreadsheet is audited`() {
    whenever(
      pricesSpreadsheetGenerator.generate(
        Supplier.SERCO,
        LocalDate.of(2021, 6, 7),
      ),
    ).thenThrow(RuntimeException("spreadsheet download failed for Serco"))

    assertThatThrownBy { service.generate(authentication, Supplier.SERCO, LocalDate.of(2021, 6, 7)) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("spreadsheet download failed for Serco")

    verify(auditService).create(
      AuditableEvent.downloadSpreadsheetFailure(
        LocalDate.of(2021, 6, 7),
        Supplier.SERCO,
        authentication,
      ),
    )
  }

  @Test
  internal fun `failure to download GEOAmey spreadsheet is audited`() {
    whenever(
      pricesSpreadsheetGenerator.generate(
        Supplier.GEOAMEY,
        LocalDate.of(2021, 6, 6),
      ),
    ).thenThrow(RuntimeException("spreadsheet download failed for GEOAmey"))

    assertThatThrownBy { service.generate(authentication, Supplier.GEOAMEY, LocalDate.of(2021, 6, 6)) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("spreadsheet download failed for GEOAmey")

    verify(auditService).create(
      AuditableEvent.downloadSpreadsheetFailure(
        LocalDate.of(2021, 6, 6),
        Supplier.GEOAMEY,
        authentication,
      ),
    )
  }
}
