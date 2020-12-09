package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PriceTest{

    @Test
    fun `Aug 31st is in previous year's effective date`(){
        val aug31_20201 = LocalDate.of(2021, 8, 31)
        assertThat(effectiveYearForDate(aug31_20201)).isEqualTo(2020)
    }

    @Test
    fun `Sept 1st is in this year's effective date`(){
        val sept1_2021 = LocalDate.of(2021, 9, 1)
        assertThat(effectiveYearForDate(sept1_2021)).isEqualTo(2021)
    }
}