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
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class SupplierReportsImporterTest(@Autowired val importService: ImportService) {

    private val from: LocalDate = LocalDate.now()

    private val to: LocalDate = from.plusDays(1)

    private val importServiceSpy: ImportService = mock {
        on { importReports(Supplier.GEOAMEY, from, to) } doAnswer { importService.importReports(Supplier.GEOAMEY, from, to) }
        on { importReports(Supplier.SERCO, from, to) } doAnswer { importService.importReports(Supplier.SERCO, from, to) }
    }

    private val importer: SupplierReportsImporter = SupplierReportsImporter(importServiceSpy)

    @Test
    internal fun `returns successful exit code when import succeeds`() {
        assertThat(importer.import(from, to).exitCode).isEqualTo(0)

        verify(importServiceSpy).importReports(Supplier.GEOAMEY, from, to)
        verify(importServiceSpy).importReports(Supplier.SERCO, from, to)
    }

    @Test
    internal fun `returns failure exit code when import fails`() {
        whenever(importServiceSpy.importReports(Supplier.GEOAMEY, from, to)).doThrow(RuntimeException())

        assertThat(importer.import(from, to).exitCode).isEqualTo(1)
    }
}
