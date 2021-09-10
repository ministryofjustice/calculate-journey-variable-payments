package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.ui.ModelMap
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.endOfMonth
import java.time.LocalDate

/**
 * Decorates the ModalMap with extension functions for the most common calls/needs in the controller classes.
 */
internal fun ModelMap.getEndOfMonth() = endOfMonth(getStartOfMonth())

internal fun ModelMap.addContractStartAndEndDates() {
  getSelectedEffectiveYear().also {
    this.addAttribute("contractualYearStart", "$it")
    this.addAttribute("contractualYearEnd", "${it + 1}")
  }
}

internal fun ModelMap.getSelectedEffectiveYear() = effectiveYearForDate(getStartOfMonth())

internal fun ModelMap.getStartOfMonth() =
  this.getAttribute(DATE_ATTRIBUTE)?.let { it as LocalDate }
    ?: throw RuntimeException("date attribute not present in model")

internal fun ModelMap.removeAnyPreviousSearchHistory() {
  this.addAttribute(PICK_UP_ATTRIBUTE, "")
  this.addAttribute(DROP_OFF_ATTRIBUTE, "")
}
