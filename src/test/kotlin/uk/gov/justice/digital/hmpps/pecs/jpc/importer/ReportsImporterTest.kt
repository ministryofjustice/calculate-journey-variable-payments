package uk.gov.justice.digital.hmpps.pecs.jpc.importer

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
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class ReportsImporterTest(@Autowired val importService: ImportService) {

    private val from: LocalDate = LocalDate.now()

    private val to: LocalDate = from.plusDays(1)

    private val importServiceSpy: ImportService = mock {
        on { importReports(from, to) } doAnswer { importService.importReports(from, to) }
    }

    private val importer: ReportsImporter = ReportsImporter(importServiceSpy)

    @Test
    internal fun `returns successful exit code when import succeeds`() {
        assertThat(importer.import(from, to).exitCode).isEqualTo(0)
        verify(importServiceSpy).importReports(from, to)
    }

    @Test
    internal fun `returns failure exit code when import fails`() {
        whenever(importServiceSpy.importReports(from, to)).doThrow(RuntimeException())
        assertThat(importer.import(from, to).exitCode).isEqualTo(1)
    }
}
