package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.web.bind.annotation.ModelAttribute

/**
 * This provides context of where the user is within the application so the primary navigation bar can be rendered
 * accordingly (in the base template file)
 */
fun interface PrimaryNavigationBar {
  @ModelAttribute("navigation")
  fun currentPrimaryNavigation() = primaryNavigationChoice().name

  fun primaryNavigationChoice(): PrimaryNavigation
}

enum class PrimaryNavigation {
  LOCATION, PRICE, SUMMARY
}
