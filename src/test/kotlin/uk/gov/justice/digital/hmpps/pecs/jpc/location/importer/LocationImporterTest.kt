package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.Schedule34LocationsProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import java.time.Clock

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class LocationImporterTest(
        @Autowired val repo: LocationRepository,
        @Autowired val clock: Clock,
        @Autowired val spreadsheetProvider: Schedule34LocationsProvider) {

    private lateinit var locationsImporter: LocationsImporter
    private lateinit var workbook: XSSFWorkbook

    @BeforeEach
    fun before() {
        locationsImporter = LocationsImporter(repo, clock, spreadsheetProvider)
        workbook = XSSFWorkbook()

        val sheetNames = listOf("QUERIES", "JPCU", "JPCNOMIS", "NOMIS", "Overview", "Courts", "Police", "Police Info",
                "Prisons", "Hospitals", "Immigration", "STC&SCH", "Other")

        val colNames = listOf("DLN", "Location Type", "Site Name", "NOMIS Agency ID")

        sheetNames.forEach {
            val sheet = workbook.createSheet(it)
            val headerRow = sheet.createRow(0)
            colNames.forEachIndexed { index, s ->
                headerRow.createCell(index).setCellValue(s)
            }
        }
    }

    @Test
    @Disabled
    fun `Assert workbook with only headers imports without errors`() {
        val errors = locationsImporter.import(workbook)

//        assertThat(errors).isEmpty()
    }

    @Test
    @Disabled
    fun `Assert empty site name returns error`() {
        val courtsSheet = workbook.getSheet(LocationsSpreadsheet.Tab.COURT.label)
        val row = courtsSheet.createRow(1)

        row.createCell(0).setCellValue("")
        row.createCell(1).setCellValue("Mag Court")
        row.createCell(2).setCellValue("") // site name
        row.createCell(3).setCellValue("NOM100")

        val errors = locationsImporter.import(workbook)

//        assertThat(errors).isNotEmpty
    }

    @AfterEach
    fun after() {
        workbook.close()
    }
}