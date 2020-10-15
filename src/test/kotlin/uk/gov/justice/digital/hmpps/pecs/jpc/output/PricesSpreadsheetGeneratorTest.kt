package uk.gov.justice.digital.hmpps.pecs.jpc.output

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceCalculator
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoameyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams

@SpringJUnitConfig(TestConfig::class)
internal class PricesSpreadsheetGeneratorTest(@Autowired private val template: JPCTemplateProvider,
                                              @Autowired private val timeSource: TimeSource,
                                              @Autowired private val sercoPricesProvider: SercoPricesProvider,
                                              @Autowired private val geoameyPricesProvider: GeoameyPricesProvider) {


    private val calculator: PriceCalculator = mock { on { allPrices(any(), any()) } doReturn listOf() }

    private val sercoProviderSpy: SercoPricesProvider = mock { on { get() } doReturn sercoPricesProvider.get() }

    private val geoProviderSpy: GeoameyPricesProvider = mock { on { get() } doReturn geoameyPricesProvider.get() }

    private val generator: PricesSpreadsheetGenerator = PricesSpreadsheetGenerator(template, timeSource, sercoProviderSpy, geoProviderSpy, calculator)

    @Test
    internal fun `verify interactions for Serco`() {
        whenever(calculator.allPrices(any(), any())).thenReturn(listOf())

        val filter = FilterParams(Supplier.SERCO, timeSource.date(), timeSource.date().plusDays(1))

        generator.generate(filter, listOf())

        verify(calculator).allPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)), eq(listOf()))
         verify(sercoProviderSpy).get()
    }

    @Test
    internal fun `verify interactions for Geoamey`() {
        whenever(calculator.allPrices(any(), any())).thenReturn(listOf())

        val filter = FilterParams(Supplier.GEOAMEY, timeSource.date(), timeSource.date().plusDays(1))

        generator.generate(filter, listOf())

        verify(calculator).allPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)), eq(listOf()))

         verify(geoProviderSpy).get()
    }
}