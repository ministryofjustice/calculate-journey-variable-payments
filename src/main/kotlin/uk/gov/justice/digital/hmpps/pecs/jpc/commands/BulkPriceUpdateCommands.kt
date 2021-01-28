package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BulkPriceUpdatesService

@ShellComponent
class BulkPriceUpdateCommands(private val bulkPriceUpdatesService: BulkPriceUpdatesService) {

  @ShellMethod("Provides the ability to perform a bulk price update for the next effective year.")
  fun bulkPriceUpdate(supplier: Supplier, multiplier: Double) {
    bulkPriceUpdatesService.bulkPriceUpdate(supplier, multiplier)
  }
}
