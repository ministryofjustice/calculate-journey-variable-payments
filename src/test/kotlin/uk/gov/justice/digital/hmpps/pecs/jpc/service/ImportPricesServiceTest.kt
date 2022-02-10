package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.price.PriceImporter
import java.time.LocalDateTime

class ImportPricesServiceTest {

  private val priceImporter: PriceImporter = mock()

  private val timeSourceWithFixedDate: TimeSource = TimeSource { LocalDateTime.of(2021, 2, 18, 12, 0, 0) }

  private val importService: ImportPricesService = ImportPricesService(priceImporter, timeSourceWithFixedDate)

  @Test
  internal fun `price importer interactions for Serco`() {
    importService.importPricesFor(Supplier.SERCO, 2020)

    verify(priceImporter).import(Supplier.SERCO, 2020)
  }

  @Test
  internal fun `price importer interactions for GEOAmey`() {
    importService.importPricesFor(Supplier.GEOAMEY, 2020)

    verify(priceImporter).import(Supplier.GEOAMEY, 2020)
  }
}
