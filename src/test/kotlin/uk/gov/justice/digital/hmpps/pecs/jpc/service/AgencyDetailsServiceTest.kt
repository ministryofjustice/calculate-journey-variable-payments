package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.NomisReferenceDataProvider

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class AgencyDetailsServiceTest(@Autowired private val referenceData: NomisReferenceDataProvider) {

  private val monitoringService: MonitoringService = mock()

  private val service = AgencyDetailsService(referenceData, monitoringService)

  @BeforeEach
  internal fun mirrorPostConstructAnnotation() {
    service.loadInMemoryNomisLocationsReferenceData()
  }

  @Test
  internal fun `AGY_LOC_ID location not found and is captured by the monitoring service`() {
    assertThat(service.findAgencyLocationNameBy("AGY_LOC_ID")).isNull()

    verify(monitoringService).capture("No match found looking up reference data for agency id 'AGY_LOC_ID'")
  }

  @Test
  internal fun `multiple agency id location names are found`() {
    assertThat(service.findAgencyLocationNameBy("THA044")).isEqualTo("MILTON KEYNES PROBATION OFFICE")
    assertThat(service.findAgencyLocationNameBy("LEEDYC")).isEqualTo("LEEDS YOUTH COURT")

    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `trimmed agency id location names are found`() {
    assertThat(service.findAgencyLocationNameBy(" THA044")).isEqualTo("MILTON KEYNES PROBATION OFFICE")
    assertThat(service.findAgencyLocationNameBy("LEEDYC ")).isEqualTo("LEEDS YOUTH COURT")

    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `mixed case agency id location names are found`() {
    assertThat(service.findAgencyLocationNameBy(" ThA044")).isEqualTo("MILTON KEYNES PROBATION OFFICE")
    assertThat(service.findAgencyLocationNameBy("LeeDYC ")).isEqualTo("LEEDS YOUTH COURT")

    verifyZeroInteractions(monitoringService)
  }

  @Test
  internal fun `failure to load reference is captured and monitored`() {
    val badData: NomisReferenceDataProvider = mock { on { get() } doThrow(RuntimeException("something went wrong")) }

    AgencyDetailsService(badData, monitoringService).loadInMemoryNomisLocationsReferenceData()

    verify(monitoringService).capture("An error occurred loading the NOMIS location reference data.")
  }
}
