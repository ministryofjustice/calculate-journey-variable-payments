package uk.gov.justice.digital.hmpps.pecs.jpc.location

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocationTypeTest {

    fun testData(): Stream<Arguments> =
            Stream.of(
                    Arguments.of("Airport", " aiRport ", "AIRPORT", LocationType.AP),
                    Arguments.of("Combined Court"," CombineD Court ", "COMBINED COURT", LocationType.CM),
                    Arguments.of("County Court"," county Court ", "COUNTY COURT", LocationType.CO),
                    Arguments.of("Crown Court"," CrowN court ", "CROWN COURT", LocationType.CC),
                    Arguments.of("Hospital"," hOspital ", "HOSPITAL", LocationType.HP),
                    Arguments.of("Immigration"," immIgration ", "IMMIGRATION", LocationType.IM),
                    Arguments.of("Mag Court"," maG courT ", "MAG COURT", LocationType.MC),
                    Arguments.of("Other"," othEr ", "OTHER", LocationType.O),
                    Arguments.of("Police"," policE ", "POLICE", LocationType.PS),
                    Arguments.of("Prison"," prisOn ", "PRISON", LocationType.PR),
                    Arguments.of("sch"," sCh ", "SCH", LocationType.SCH),
                    Arguments.of("stc"," stC ", "STC", LocationType.STC)
            )

    @ParameterizedTest
    @MethodSource("testData")
    fun `test all locations map exact, mixed with whitespace and all uppercase` (exact: String, mixedCaseAndWhiteSpace: String, allUpperCase: String, expected: LocationType) {
        assertThat(LocationType.map(exact)).isEqualTo(expected)
        assertThat(LocationType.map(mixedCaseAndWhiteSpace)).isEqualTo(expected)
        assertThat(LocationType.map(allUpperCase)).isEqualTo(expected)
    }

    @Test
    fun `unrecognised location type is not mapped`() {
        assertThat(LocationType.map("garbage")).isNull()
    }
}
