package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.core.io.InputStreamResource
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.SpreadsheetProtection
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletResponse


@RestController
class ImportController(private val importService: ImportService,
                       private val timeSource: TimeSource,
                       private val spreadsheetProtection: SpreadsheetProtection) {


    @GetMapping("/import-locations")
    fun importLocations(
            response: HttpServletResponse?): String {

        importService.importLocations()
        return "Done import locations"
    }

    @GetMapping("/import-prices/{supplier}")
    fun importPrices(
            @PathVariable supplier: String,
            response: HttpServletResponse?): String {

        importService.importPrices(Supplier.valueOfCaseInsensitive(supplier))

        return "Done import prices for $supplier"
    }

    @GetMapping("/import-reports/{supplier}")
    fun importReports(
            @PathVariable supplier: String,
            @RequestParam(name = "reports_from", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) reportsFrom: LocalDate,
            @RequestParam(name = "reports_to", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) reportsTo: LocalDate,
            response: HttpServletResponse?): String {

        importService.importReports(supplier, reportsFrom, reportsTo)

        return "Done import reports for $supplier"
    }

    @GetMapping("/generate-prices-spreadsheet/{supplier}")
    @Throws(IOException::class)
    fun generateSpreadsheet(
            @PathVariable supplier: String,
            @RequestParam(name = "moves_from", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) movesFrom: LocalDate,
            @RequestParam(name = "moves_to", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) movesTo: LocalDate,
            response: HttpServletResponse?): ResponseEntity<InputStreamResource?>? {

        return importService.spreadsheet(supplier, movesFrom, movesTo)?.let { file ->
            val uploadDateTime = timeSource.dateTime().format(DateTimeFormatter.ofPattern("YYYY-MM-dd_HH_mm"))
            val filename = "Journey_Variable_Payment_Output_${supplier}_${uploadDateTime}.xlsx"
            val mediaType: MediaType = MediaType.parseMediaType("application/vnd.ms-excel")
            val protectedFile = spreadsheetProtection.protectAndGet(file)
            val resource = InputStreamResource(FileInputStream(protectedFile))

            ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=$filename")
                    .contentType(mediaType)
                    .contentLength(protectedFile.length())
                    .body(resource)
        } ?: ResponseEntity.noContent().build()
    }
}
