package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class LocationImporterTest(
        @Autowired val eventPublisher: ApplicationEventPublisher,
        @Autowired val repo: LocationRepository) {

    private lateinit var locationsImporter: LocationsImporter
    private lateinit var workbook: XSSFWorkbook

    @BeforeEach
    fun before() {
        locationsImporter = LocationsImporter(repo, eventPublisher)
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
    fun `Assert workbook with only headers imports without errors`() {
        val errors = locationsImporter.import(workbook)

        assertThat(errors).isEmpty()
    }

    @Test
    fun `Assert empty site name returns error`() {
        val courtsSheet = workbook.getSheetAt(Court.index)
        val row = courtsSheet.createRow(1)

        row.createCell(0).setCellValue("")
        row.createCell(1).setCellValue("Mag Court")
        row.createCell(2).setCellValue("") // site name
        row.createCell(3).setCellValue("NOM100")

        val errors = locationsImporter.import(workbook)

        assertThat(errors).isNotEmpty
    }

    @AfterEach
    fun after() {
        workbook.close()
    }
}