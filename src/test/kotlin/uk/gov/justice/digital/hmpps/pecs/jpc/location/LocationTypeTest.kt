package uk.gov.justice.digital.hmpps.pecs.jpc.location

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocationTypeTest {

    @Test
    fun `airport location type is mapped`() {
        assertThat(LocationType.map("Airport")).isEqualTo(LocationType.AP)
        assertThat(LocationType.map(" aiRport ")).isEqualTo(LocationType.AP)
        assertThat(LocationType.map("AIRPORT")).isEqualTo(LocationType.AP)
    }

    @Test
    fun `combined court location type is mapped`() {
        assertThat(LocationType.map("Combined Court")).isEqualTo(LocationType.CM)
        assertThat(LocationType.map(" CombineD Court ")).isEqualTo(LocationType.CM)
        assertThat(LocationType.map("COMBINED COURT")).isEqualTo(LocationType.CM)
    }

    @Test
    fun `county court location type is mapped`() {
        assertThat(LocationType.map("County Court")).isEqualTo(LocationType.CO)
        assertThat(LocationType.map(" county Court ")).isEqualTo(LocationType.CO)
        assertThat(LocationType.map("COUNTY COURT")).isEqualTo(LocationType.CO)
    }

    @Test
    fun `crown court location type is mapped`() {
        assertThat(LocationType.map("Crown Court")).isEqualTo(LocationType.CC)
        assertThat(LocationType.map(" CrowN court ")).isEqualTo(LocationType.CC)
        assertThat(LocationType.map("CROWN COURT")).isEqualTo(LocationType.CC)
    }

    @Test
    fun `hospital location type is mapped`() {
        assertThat(LocationType.map("Hospital")).isEqualTo(LocationType.HP)
        assertThat(LocationType.map(" hOspital ")).isEqualTo(LocationType.HP)
        assertThat(LocationType.map("HOSPITAL")).isEqualTo(LocationType.HP)
    }

    @Test
    fun `immigration location type is mapped`() {
        assertThat(LocationType.map("Immigration")).isEqualTo(LocationType.IM)
        assertThat(LocationType.map(" immigration ")).isEqualTo(LocationType.IM)
        assertThat(LocationType.map("IMMIGRATION")).isEqualTo(LocationType.IM)
    }

    @Test
    fun `magistrates court location type is mapped`() {
        assertThat(LocationType.map("Mag Court")).isEqualTo(LocationType.MC)
        assertThat(LocationType.map(" maG courT ")).isEqualTo(LocationType.MC)
        assertThat(LocationType.map("MAG COURT")).isEqualTo(LocationType.MC)
    }

    @Test
    fun `other location type is mapped`() {
        assertThat(LocationType.map("Other")).isEqualTo(LocationType.O)
        assertThat(LocationType.map(" other ")).isEqualTo(LocationType.O)
        assertThat(LocationType.map("OTHER")).isEqualTo(LocationType.O)
    }

    @Test
    fun `police location type is mapped`() {
        assertThat(LocationType.map("Police")).isEqualTo(LocationType.PS)
        assertThat(LocationType.map(" police ")).isEqualTo(LocationType.PS)
        assertThat(LocationType.map("POLICE")).isEqualTo(LocationType.PS)
    }

    @Test
    fun `prison location type is mapped`() {
        assertThat(LocationType.map("Prison")).isEqualTo(LocationType.PR)
        assertThat(LocationType.map(" prison ")).isEqualTo(LocationType.PR)
        assertThat(LocationType.map("PRISON")).isEqualTo(LocationType.PR)
    }

    @Test
    fun `secure children's home location type is mapped`() {
        assertThat(LocationType.map("SCH")).isEqualTo(LocationType.SCH)
        assertThat(LocationType.map(" sch ")).isEqualTo(LocationType.SCH)
    }

    @Test
    fun `secure training centre location type is mapped`() {
        assertThat(LocationType.map("STC")).isEqualTo(LocationType.STC)
        assertThat(LocationType.map(" stc ")).isEqualTo(LocationType.STC)
    }

    @Test
    fun `location types are not mapped`() {
        assertThat(LocationType.map("Alrport")).isNull()
        assertThat(LocationType.map("C0MBINED COURT")).isNull()
        assertThat(LocationType.map("C0UNTY COURT")).isNull()
        assertThat(LocationType.map("CR0WN Court")).isNull()
        assertThat(LocationType.map("H0spital")).isNull()
        assertThat(LocationType.map("IMMIGRATI0N")).isNull()
        assertThat(LocationType.map("MAGISTRATES COURT")).isNull()
        assertThat(LocationType.map("0ther")).isNull()
        assertThat(LocationType.map("Pris0n")).isNull()
        assertThat(LocationType.map("P0l1ce")).isNull()
        assertThat(LocationType.map("SCHH")).isNull()
        assertThat(LocationType.map("STCC")).isNull()
    }
}
