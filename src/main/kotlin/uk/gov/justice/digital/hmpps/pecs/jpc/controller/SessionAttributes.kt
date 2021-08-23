package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.ui.ModelMap
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.endOfMonth
import java.time.LocalDate

const val PICK_UP_ATTRIBUTE = "pick-up"
const val DROP_OFF_ATTRIBUTE = "drop-off"
const val DATE_ATTRIBUTE = "date"
const val SUPPLIER_ATTRIBUTE = "supplier"
const val START_OF_MONTH_DATE_ATTRIBUTE = "startOfMonthDate"
const val END_OF_MONTH_DATE_ATTRIBUTE = "endOfMonthDate"
const val MOVE_ATTRIBUTE = "move"

internal fun ModelMap.getEndOfMonth() = endOfMonth(getStartOfMonth())

internal fun ModelMap.getStartOfMonth() =
  this.getAttribute(DATE_ATTRIBUTE)?.let { it as LocalDate }
    ?: throw RuntimeException("date attribute not present in model")

internal fun ModelMap.getEffectiveYear() = effectiveYearForDate(getStartOfMonth())
