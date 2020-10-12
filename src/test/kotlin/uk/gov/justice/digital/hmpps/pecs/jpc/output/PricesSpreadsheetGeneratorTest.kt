package uk.gov.justice.digital.hmpps.pecs.jpc.output

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceCalculator
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JCPTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import java.time.Clock
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class PricesSpreadsheetGeneratorTest(@Autowired private val template: JCPTemplateProvider,
                                              @Autowired private val clock: Clock,
                                              @Autowired private val sercoPricesProvider: SercoPricesProvider,
                                              @Autowired private val geoamyPricesProvider: GeoamyPricesProvider) {

    private val calculator: PriceCalculator = mock()

    private val sercoProviderSpy: SercoPricesProvider = mock { on { get() } doReturn sercoPricesProvider.get() }

    private val geoProviderSpy: GeoamyPricesProvider = mock { on { get() } doReturn geoamyPricesProvider.get() }

    private val generator: PricesSpreadsheetGenerator = PricesSpreadsheetGenerator(template, clock, sercoProviderSpy, geoProviderSpy)

    @Test
    internal fun `verify interactions for Serco`() {
        whenever(calculator.standardPrices(any())).thenReturn(sequenceOf())
        whenever(calculator.redirectionPrices(any())).thenReturn(sequenceOf())
        whenever(calculator.longHaulPrices(any())).thenReturn(sequenceOf())
        whenever(calculator.lockoutPrices(any())).thenReturn(sequenceOf())
        whenever(calculator.multiTypePrices(any())).thenReturn(sequenceOf())

        val filter = FilterParams(Supplier.SERCO, LocalDate.now(clock), LocalDate.now(clock).plusDays(1))

        generator.generate(filter, calculator)

        verify(calculator).standardPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(calculator).redirectionPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(calculator).longHaulPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(calculator).lockoutPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(calculator).multiTypePrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(sercoProviderSpy).get()
    }

    @Test
    internal fun `verify interactions for Geoamey`() {
        whenever(calculator.standardPrices(any())).thenReturn(sequenceOf())
        whenever(calculator.redirectionPrices(any())).thenReturn(sequenceOf())
        whenever(calculator.longHaulPrices(any())).thenReturn(sequenceOf())
        whenever(calculator.lockoutPrices(any())).thenReturn(sequenceOf())
        whenever(calculator.multiTypePrices(any())).thenReturn(sequenceOf())

        val filter = FilterParams(Supplier.GEOAMEY, LocalDate.now(clock), LocalDate.now(clock).plusDays(1))

        generator.generate(filter, calculator)

        verify(calculator).standardPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(calculator).redirectionPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(calculator).longHaulPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(calculator).lockoutPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(calculator).multiTypePrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)))
        verify(geoProviderSpy).get()
    }
}