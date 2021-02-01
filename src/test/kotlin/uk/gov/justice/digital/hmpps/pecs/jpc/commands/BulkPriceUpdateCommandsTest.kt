package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BulkPricesService

internal class BulkPriceUpdateCommandsTest {

  private val service: BulkPricesService = mock()
  private val command: BulkPriceUpdateCommands = BulkPriceUpdateCommands(service)

  @Test
  internal fun `service invoked for Serco with expected multiplier`() {
    command.addNextYearsPrices(Supplier.SERCO, 1.5)

    verify(service).addNextYearsPrices(Supplier.SERCO, 1.5)
  }

  @Test
  internal fun `service invoked for Geoamey with expected multiplier`() {
    command.addNextYearsPrices(Supplier.GEOAMEY, 2.0)

    verify(service).addNextYearsPrices(Supplier.GEOAMEY, 2.0)
  }
}
