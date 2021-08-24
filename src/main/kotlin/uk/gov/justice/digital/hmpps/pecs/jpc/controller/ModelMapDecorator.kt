package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.ui.ModelMap
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.endOfMonth
import java.time.LocalDate

internal fun ModelMap.getEndOfMonth() = endOfMonth(getStartOfMonth())

internal fun ModelMap.addContractStartAndEndDates() {
  getEffectiveYear().also {
    this.addAttribute("contractualYearStart", "$it")
    this.addAttribute("contractualYearEnd", "${it + 1}")
  }
}

internal fun ModelMap.getEffectiveYear() = effectiveYearForDate(getStartOfMonth())

internal fun ModelMap.getStartOfMonth() =
  this.getAttribute(DATE_ATTRIBUTE)?.let { it as LocalDate }
    ?: throw RuntimeException("date attribute not present in model")

internal fun ModelMap.removeAnyPreviousSearchHistory() {
  this.addAttribute(PICK_UP_ATTRIBUTE, "")
  this.addAttribute(DROP_OFF_ATTRIBUTE, "")
}
