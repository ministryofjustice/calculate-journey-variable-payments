package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.SessionAttributes
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.SpreadsheetService
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletResponse

@RestController
@SessionAttributes(HtmlController.SUPPLIER_ATTRIBUTE)
class OutputSpreadsheetController(
  private val spreadsheetService: SpreadsheetService,
  private val timeSource: TimeSource
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping("/generate-prices-spreadsheet/{supplier}")
  @Throws(IOException::class)
  fun generateSpreadsheet(
    @PathVariable supplier: String,
    @RequestParam(
      name = "moves_from",
      required = true
    ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) movesFrom: LocalDate,
    response: HttpServletResponse?,
    authentication: Authentication?
  ): ResponseEntity<InputStreamResource?>? {
    logger.info("getting spreadsheet for $supplier")

    return spreadsheetService.spreadsheet(authentication!!, Supplier.valueOfCaseInsensitive(supplier), movesFrom)?.let { file ->
      val uploadDateTime = timeSource.dateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm"))
      val filename = "Journey_Variable_Payment_Output_${Supplier.valueOfCaseInsensitive(supplier)}_$uploadDateTime.xlsx"
      val mediaType: MediaType = MediaType.parseMediaType("application/vnd.ms-excel")
      val resource = InputStreamResource(FileInputStream(file))

      ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=$filename")
        .contentType(mediaType)
        .contentLength(file.length())
        .body(resource)
    } ?: ResponseEntity.noContent().build()
  }
}
