package uk.gov.justice.digital.hmpps.pecs.jpc.output

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.PriceCalculator
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JCPTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import java.time.Clock
import java.time.LocalDate

@SpringJUnitConfig(TestConfig::class)
internal class PricesSpreadsheetGeneratorTest(@Autowired private val template: JCPTemplateProvider,
                                              @Autowired private val clock: Clock,
                                              @Autowired private val sercoPricesProvider: SercoPricesProvider,
                                              @Autowired private val geoamyPricesProvider: GeoamyPricesProvider) {


    private val calculator: PriceCalculator = mock { on { allPrices(any(), any()) } doReturn listOf() }

    private val sercoProviderSpy: SercoPricesProvider = mock { on { get() } doReturn sercoPricesProvider.get() }

    private val geoProviderSpy: GeoamyPricesProvider = mock { on { get() } doReturn geoamyPricesProvider.get() }

    private val generator: PricesSpreadsheetGenerator = PricesSpreadsheetGenerator(template, clock, sercoProviderSpy, geoProviderSpy, calculator)

    @Test
    internal fun `verify interactions for Serco`() {
        whenever(calculator.allPrices(any(), any())).thenReturn(listOf())

        val filter = FilterParams(Supplier.SERCO, LocalDate.now(clock), LocalDate.now(clock).plusDays(1))

        generator.generate(filter, listOf())

        verify(calculator).allPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)), eq(listOf()))
         verify(sercoProviderSpy).get()
    }

    @Test
    internal fun `verify interactions for Geoamey`() {
        whenever(calculator.allPrices(any(), any())).thenReturn(listOf())

        val filter = FilterParams(Supplier.GEOAMEY, LocalDate.now(clock), LocalDate.now(clock).plusDays(1))

        generator.generate(filter, listOf())

        verify(calculator).allPrices(eq(FilterParams(filter.supplier, filter.movesFrom, filter.movesTo)), eq(listOf()))

         verify(geoProviderSpy).get()
    }
}