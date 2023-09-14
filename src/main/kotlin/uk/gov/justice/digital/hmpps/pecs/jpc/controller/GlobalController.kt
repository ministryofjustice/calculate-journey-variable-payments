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

  @Value("\${HMPPS_AUTH_BASE_URI}")
  private lateinit var hmppsUri: String

  @ModelAttribute("feedbackUrl")
  fun feedbackUrl() = feedbackUrl.ifBlankStayOrElseGet()

  @ModelAttribute("hmppsUrl")
  fun hmppsUrl() = hmppsUri.ifBlankStayOrElseGet()

  @ModelAttribute("manageYourDetailsUrl")
  fun manageYourDetailsUrl() = hmppsUri.ifBlankStayOrElseGet("/auth/account-details")

  fun String.ifBlankStayOrElseGet(suffix: String? = null) =
    if (this.isBlank()) '#' else suffix?.let { this.plus(suffix) } ?: this
}
