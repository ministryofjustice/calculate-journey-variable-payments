package uk.gov.justice.digital.hmpps.pecs.jpc.import

import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService

internal class LocationAndPriceImporterTest {

    private val priceRepository: PriceRepository = mock()
    private val locationRepository: LocationRepository = mock()
    private val importService: ImportService = mock()
    private val importer: LocationAndPriceImporter = LocationAndPriceImporter(priceRepository, locationRepository, importService)

    @Test
    internal fun `returns successful exit code when import succeeds`() {
        assertThat(importer.exitCode).isEqualTo(0)
        verify(priceRepository).deleteAll()
        verify(locationRepository).deleteAll()
        verify(importService).importLocations()
        verify(importService).importPrices(Supplier.GEOAMEY)
        verify(importService).importPrices(Supplier.SERCO)
    }

    @Test
    internal fun `returns failure exit code when import fails`() {
        whenever(importService.importLocations()).doThrow(RuntimeException())

        assertThat(importer.exitCode).isEqualTo(1)
    }
}
