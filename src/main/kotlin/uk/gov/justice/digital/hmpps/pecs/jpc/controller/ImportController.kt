package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.io.FileInputStream
import java.io.IOException
import javax.servlet.http.HttpServletResponse


@RestController
class ImportController(private val importService: ImportService) {

    @PostMapping("/locations/import")
    fun locations(): String = importService.importLocations().second.name

    @PostMapping("/prices/import")
    fun prices(): String = importService.importPrices().second.name

    @GetMapping("/spreadsheet/{supplier}")
    @Throws(IOException::class)
    fun spreadsheet(@PathVariable supplier: String, response: HttpServletResponse?): ResponseEntity<InputStreamResource?>? {

        return importService.spreadsheet(supplier)?.let { file ->
            val mediaType: MediaType = MediaType.parseMediaType("application/vnd.ms-excel")
            val resource = InputStreamResource(FileInputStream(file))
            ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=$supplier-prices.xlsx") // Content-Type
                    .contentType(mediaType)
                    .contentLength(file.length())
                    .body(resource)
        } ?: ResponseEntity.noContent().build()
    }
}
