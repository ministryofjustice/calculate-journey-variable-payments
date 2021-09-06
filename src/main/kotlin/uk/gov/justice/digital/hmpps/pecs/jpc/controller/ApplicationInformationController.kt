package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.SessionAttributes
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AnnualPriceAdjustmentsService

@RestController
@RequestMapping(name = "Supplier information", path = ["/app"], produces = [MediaType.APPLICATION_JSON_VALUE])
@SessionAttributes(SUPPLIER_ATTRIBUTE)
class ApplicationInformationController(val annualPriceAdjustmentsService: AnnualPriceAdjustmentsService) {
  private val logger = LoggerFactory.getLogger(javaClass)

  @GetMapping(path = ["/info"])
  fun info(@ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier): ApplicationInformation {
    logger.info("getting info for $supplier")

    return ApplicationInformation(
      when {
        annualPriceAdjustmentsService.adjustmentInProgressFor(supplier) ->
          "A bulk price adjustment is currently in progress. Any further price changes will be prevented until the adjustment is complete."
        else -> null
      }
    )
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class ApplicationInformation(val message: String? = null)
}
