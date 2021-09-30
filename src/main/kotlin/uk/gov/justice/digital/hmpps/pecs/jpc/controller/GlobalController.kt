package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * Applies global level access/information to all controllers where such things are needed.
 */
@ControllerAdvice
class GlobalController {

  @Value("\${FEEDBACK_URL:#}")
  private lateinit var feedbackUrl: String

  @Value("\${FEATURE_FLAG_PRICE_EXCEPTIONS:false}")
  private lateinit var priceExceptionFeatureEnabled: String

  @ModelAttribute("feedbackUrl")
  fun feedbackUrl() = feedbackUrl.ifBlank { '#' }

  @ModelAttribute("priceExceptionFeatureEnabled")
  fun priceExceptionsFeatureEnabled() = priceExceptionFeatureEnabled.toBoolean()
}
