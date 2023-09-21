package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.SessionAttribute
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.JourneyPriceCatalogueService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val logger = loggerFor<JourneyPriceCatalogueController>()

/**
 * Controller responsible for generating and returning an on demand journey price catalogue spreadsheet to the end user.
 */
@RestController
class JourneyPriceCatalogueController(
  private val journeyPriceCatalogueService: JourneyPriceCatalogueService,
  private val timeSource: TimeSource,
) {

  @GetMapping(GENERATE_PRICES_SPREADSHEET)
  @Throws(IOException::class)
  fun generateJourneyPriceCatalogue(
    @SessionAttribute(name = SUPPLIER_ATTRIBUTE, required = false) supplier: Supplier?,
    @SessionAttribute(name = DATE_ATTRIBUTE, required = false) movesFrom: LocalDate?,
    response: HttpServletResponse?,
  ): ResponseEntity<InputStreamResource?>? {
    if (supplier == null || movesFrom == null) {
      logger.info("Missing session attributes, no session or session has expired.")

      return ResponseEntity.noContent().build()
    }

    logger.info("getting spreadsheet for $supplier")

    return journeyPriceCatalogueService.generate(SecurityContextHolder.getContext().authentication, supplier, movesFrom)
      ?.let { file ->
        val uploadDateTime = timeSource.dateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm"))
        val filename =
          "Journey_Variable_Payment_Output_${supplier}_$uploadDateTime.xlsx"
        val mediaType: MediaType = MediaType.parseMediaType("application/vnd.ms-excel")
        val resource = InputStreamResource(FileInputStream(file))

        ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=$filename")
          .contentType(mediaType)
          .contentLength(file.length())
          .body(resource)
      } ?: ResponseEntity.noContent().build()
  }

  companion object {
    const val GENERATE_PRICES_SPREADSHEET = "/generate-prices-spreadsheet"
  }
}
