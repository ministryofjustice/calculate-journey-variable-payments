package uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.justice.digital.hmpps.pecs.jpc.util.MonthYearParser

class MonthYearConstraintValidator : ConstraintValidator<ValidMonthYear?, String?> {
  override fun initialize(arg0: ValidMonthYear?) {}
  override fun isValid(dateAsString: String?, context: ConstraintValidatorContext): Boolean = dateAsString != null && MonthYearParser.isValid(dateAsString)
}
