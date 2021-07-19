package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService

internal class BulkPriceImportCommandTest {

  private val importService: ImportService = mock()

  private val effectiveYear: EffectiveYear = mock { on { current() } doReturn 2020 }

  private val command: BulkPriceImportCommand = BulkPriceImportCommand(importService, effectiveYear)

  @Test
  internal fun `import prices for Serco prior to 2019 fails`() {
    assertThatThrownBy { command.bulkImportPricesFor(Supplier.SERCO, 2018) }.isInstanceOf(RuntimeException::class.java)
  }

  @Test
  internal fun `import prices for GEOAmey prior to 2019 fails`() {
    assertThatThrownBy { command.bulkImportPricesFor(Supplier.GEOAMEY, 2018) }.isInstanceOf(RuntimeException::class.java)
  }

  @Test
  internal fun `import prices for Serco for 2019 succeeds`() {
    command.bulkImportPricesFor(Supplier.SERCO, 2019)

    verify(importService).importPrices(Supplier.SERCO)
  }

  @Test
  internal fun `import prices for Geoamey for 2019 succeeds`() {
    command.bulkImportPricesFor(Supplier.GEOAMEY, 2019)

    verify(importService).importPrices(Supplier.GEOAMEY)
  }

  @Test
  internal fun `import prices for Serco for the current effective year (2020) succeeds`() {
    command.bulkImportPricesFor(Supplier.SERCO, 2020)

    verify(importService).importPrices(Supplier.SERCO)
  }

  @Test
  internal fun `import prices for Geoamey for the current effective year (2020) succeeds`() {
    command.bulkImportPricesFor(Supplier.GEOAMEY, 2020)

    verify(importService).importPrices(Supplier.GEOAMEY)
  }

  @Test
  internal fun `import prices for Serco for the effective year 2021 fails`() {
    assertThatThrownBy { command.bulkImportPricesFor(Supplier.SERCO, 2021) }.isInstanceOf(RuntimeException::class.java)
  }

  @Test
  internal fun `import prices for Geoamey for the effective year 2021 fails`() {
    assertThatThrownBy { command.bulkImportPricesFor(Supplier.GEOAMEY, 2021) }.isInstanceOf(RuntimeException::class.java)
  }

  @Test
  internal fun `import prices for Unknown for any valid year fails`() {
    assertThatThrownBy { command.bulkImportPricesFor(Supplier.UNKNOWN, 2019) }.isInstanceOf(RuntimeException::class.java)
  }
}
