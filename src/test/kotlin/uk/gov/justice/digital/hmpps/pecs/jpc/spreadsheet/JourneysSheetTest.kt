package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.move.*


@SpringJUnitConfig(TestConfig::class)
internal class JourneysSheetTest(@Autowired private val template: JPCTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    @Test
    internal fun `test unique journeys`() {

        val journey = UniqueJourney(
              fromNomisAgencyId = "FRO",
                fromLocationType = LocationType.PR,
                fromSiteName = "from",
                toNomisAgencyId = "TO",
                toLocationType = null,
                toSiteName = null,
                unitPriceInPence = 100,
                volume = 22,
                totalPriceInPence = 2200
        )
        
        val sheet = JourneysSheet(workbook, PriceSheet.Header(moveDate, ClosedRangeLocalDate(moveDate, moveDate), Supplier.SERCO))
        sheet.writeJourneys(listOf(journey))

        assertCellEquals(sheet, 10, 0, "from") // from site name
        assertCellEquals(sheet, 10, 1, "TO") // TO - NOMIS Agency ID because there is no site name
        assertCellEquals(sheet, 10, 2, 22.0) // volume
        assertCellEquals(sheet, 10, 3, 1.0) // unit price in pounds
        assertCellEquals(sheet, 10, 4, 22.0) // total price in pounds
    }
}