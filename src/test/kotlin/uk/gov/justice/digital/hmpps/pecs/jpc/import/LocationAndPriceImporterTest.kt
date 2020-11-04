package uk.gov.justice.digital.hmpps.pecs.jpc.import

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class LocationAndPriceImporterTest(@Autowired val priceRepository: PriceRepository,
                                            @Autowired val locationRepository: LocationRepository,
                                            @Autowired val importService: ImportService) {

    private val priceRepositorySpy: PriceRepository = mock { on { deleteAll() } doAnswer { priceRepository.deleteAll() } }

    private val locationRepositorySpy: LocationRepository = mock { on { deleteAll() } doAnswer { locationRepository.deleteAll() } }

    private val importServiceSpy: ImportService = mock {
        on { importLocations() } doAnswer { importService.importLocations() }
        on { importPrices(Supplier.GEOAMEY) } doAnswer { importService.importPrices(Supplier.GEOAMEY) }
        on { importPrices(Supplier.SERCO) } doAnswer { importService.importPrices(Supplier.SERCO) }
    }

    private val importer: LocationAndPriceImporter = LocationAndPriceImporter(priceRepositorySpy, locationRepositorySpy, importServiceSpy)

    @Test
    internal fun `returns successful exit code when import succeeds`() {
        assertThat(priceRepository.count()).isEqualTo(0)
        assertThat(locationRepository.count()).isEqualTo(0)
        assertThat(importer.import().exitCode).isEqualTo(0)

        verify(priceRepositorySpy).deleteAll()
        verify(locationRepositorySpy).deleteAll()
        verify(importServiceSpy).importLocations()
        verify(importServiceSpy).importPrices(Supplier.GEOAMEY)
        verify(importServiceSpy).importPrices(Supplier.SERCO)

        assertThat(locationRepository.count()).isEqualTo(2)
        assertThat(priceRepository.count()).isEqualTo(2)
    }

    @Test
    internal fun `returns failure exit code when import fails`() {
        whenever(importServiceSpy.importLocations()).doThrow(RuntimeException())

        assertThat(importer.import().exitCode).isEqualTo(1)
    }
}
