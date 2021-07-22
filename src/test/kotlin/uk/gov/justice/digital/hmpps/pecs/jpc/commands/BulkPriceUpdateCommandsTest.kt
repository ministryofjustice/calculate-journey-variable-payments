package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BulkPricesService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.PriceUpliftService

internal class BulkPriceUpdateCommandsTest {

  private val bulkPricesService: BulkPricesService = mock()
  private val priceUpliftService: PriceUpliftService = mock()
  private val command: BulkPriceUpdateCommands = BulkPriceUpdateCommands(bulkPricesService, priceUpliftService)

  @Test
  internal fun `service invoked for Serco with expected multiplier`() {
    command.addNextYearsPrices(Supplier.SERCO, 1.5)

    verify(bulkPricesService).addNextYearsPrices(Supplier.SERCO, 1.5)
  }

  @Test
  internal fun `service invoked for Geoamey with expected multiplier`() {
    command.addNextYearsPrices(Supplier.GEOAMEY, 2.0)

    verify(bulkPricesService).addNextYearsPrices(Supplier.GEOAMEY, 2.0)
  }
}
