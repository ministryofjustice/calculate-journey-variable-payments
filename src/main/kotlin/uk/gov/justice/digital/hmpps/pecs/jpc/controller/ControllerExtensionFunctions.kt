package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.endOfMonth
import java.time.LocalDate

/**
 * Contains reusable extension functions for the controllers.
 */

internal val XSS_CHARACTERS = setOf('<', '＜', '〈', '〈', '>', '＞', '〉', '〉')

@JvmInline
internal value class MonthYear(private val monthYear: Pair<String, Int>) {
  fun month() = monthYear.first

  fun year() = monthYear.second
}

internal fun BindingResult.rejectIfContainsInvalidCharacters(value: String, field: String, errorCode: String) {
  if (value.any(XSS_CHARACTERS::contains)) {
    this.rejectValue(
      field,
      errorCode,
      "The following characters ${XSS_CHARACTERS.joinToString(separator = "', '", prefix = "'", postfix = "'")} are not allowed.",
    )
  }
}

internal fun ModelMap.getEndOfMonth() = endOfMonth(getStartOfMonth())

internal fun ModelMap.addContractStartAndEndDates() {
  getSelectedEffectiveYear().also {
    this.addAttribute("contractualYearStart", "$it")
    this.addAttribute("contractualYearEnd", "${it + 1}")
  }
}

internal fun ModelMap.getSelectedYearStart() = MonthYear("September" to getSelectedEffectiveYear())

internal fun ModelMap.getSelectedYearEnd() = MonthYear("August" to getSelectedEffectiveYear() + 1)

internal fun ModelMap.getSelectedEffectiveYear() = effectiveYearForDate(getStartOfMonth())

internal fun ModelMap.getStartOfMonth() = this.getAttribute(DATE_ATTRIBUTE)?.let { it as LocalDate }
  ?: throw RuntimeException("date attribute not present in model")

internal fun ModelMap.removeAnyPreviousSearchHistory() {
  this.addAttribute(PICK_UP_ATTRIBUTE, "")
  this.addAttribute(DROP_OFF_ATTRIBUTE, "")
}

internal fun String.titleCased() = this.lowercase().replaceFirstChar { it.titlecaseChar() }
