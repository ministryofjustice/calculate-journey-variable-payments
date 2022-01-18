package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.time.LocalDateTime

/**
 * Provides CLI command to add the latest supplier prices spreadsheet(s) to the price book.
 *
 * IMPORTANT: this should be run in a pre-production environment prior to running in production.
 *
 * The price spreadsheets are pulled from an Amazon S3 bucket. These are uploaded manually, it is important you are
 * confident you have the correct spreadsheet(s) in S3 prior to running this command.
 */
internal class BulkPriceImportCommandTest {

  private val september2020 = LocalDateTime.of(2020, 9, 1, 0, 0)

  private val importService: ImportService = mock()

  private val effectiveYear: EffectiveYear = EffectiveYear { september2020 }

  private val command: BulkPriceImportCommand = BulkPriceImportCommand(importService, effectiveYear)

  @Test
  internal fun `import prices for Serco fails is effective year is two years or more out of date`() {
    assertThatThrownBy { command.bulkImportPricesFor(Supplier.SERCO, 2018) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("Price imports can only take place in the current '2020' or previous effective year '2019'.")
  }

  @Test
  internal fun `import prices for GEOAmey fails is effective year is two years or more out of date`() {
    assertThatThrownBy { command.bulkImportPricesFor(Supplier.GEOAMEY, 2018) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("Price imports can only take place in the current '2020' or previous effective year '2019'.")
  }

  @Test
  internal fun `import prices for Serco for 2019 succeeds`() {
    command.bulkImportPricesFor(Supplier.SERCO, 2019)

    verify(importService).importPrices(Supplier.SERCO, 2019)
  }

  @Test
  internal fun `import prices for Geoamey for 2019 succeeds`() {
    command.bulkImportPricesFor(Supplier.GEOAMEY, 2019)

    verify(importService).importPrices(Supplier.GEOAMEY, 2019)
  }

  @Test
  internal fun `import prices for Serco for the current effective year (2020) succeeds`() {
    command.bulkImportPricesFor(Supplier.SERCO, 2020)

    verify(importService).importPrices(Supplier.SERCO, 2020)
  }

  @Test
  internal fun `import prices for Geoamey for the current effective year (2020) succeeds`() {
    command.bulkImportPricesFor(Supplier.GEOAMEY, 2020)

    verify(importService).importPrices(Supplier.GEOAMEY, 2020)
  }

  @Test
  internal fun `import prices for Serco for the effective year in the future fails`() {
    assertThatThrownBy { command.bulkImportPricesFor(Supplier.SERCO, 2021) }.isInstanceOf(RuntimeException::class.java)
  }

  @Test
  internal fun `import prices for Geoamey for the effective year in the future fails`() {
    assertThatThrownBy {
      command.bulkImportPricesFor(
        Supplier.GEOAMEY,
        2021
      )
    }.isInstanceOf(RuntimeException::class.java)
  }

  @Test
  internal fun `import prices for Unknown for any valid year fails`() {
    assertThatThrownBy {
      command.bulkImportPricesFor(
        Supplier.UNKNOWN,
        2019
      )
    }.isInstanceOf(RuntimeException::class.java)
  }
}
