package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.core.io.InputStreamResource
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalDate
import javax.servlet.http.HttpServletResponse


@RestController
class ImportController(private val importService: ImportService) {

    @PostMapping("/locations/import")
    fun locations(): String = importService.importLocations().second.name

    @PostMapping("/prices/import")
    fun prices(): String = importService.importPrices().second.name

    @GetMapping("/generate-prices-spreadsheet/{supplier}")
    @Throws(IOException::class)
    fun generateSpreadsheet(
        @PathVariable supplier: String,
        @RequestParam(name = "moves_from", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) movesFrom: LocalDate,
        @RequestParam(name = "moves_to", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) movesTo: LocalDate,
        @RequestParam(name = "reports_to", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) reportsTo: LocalDate,
            response: HttpServletResponse?): ResponseEntity<InputStreamResource?>? {

        return importService.spreadsheet(supplier, movesFrom, movesTo, reportsTo)?.let { file ->
            val mediaType: MediaType = MediaType.parseMediaType("application/vnd.ms-excel")
            val resource = InputStreamResource(FileInputStream(file))
            ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=$supplier-prices.xlsx")
                    .contentType(mediaType)
                    .contentLength(file.length())
                    .body(resource)
        } ?: ResponseEntity.noContent().build()
    }
}
