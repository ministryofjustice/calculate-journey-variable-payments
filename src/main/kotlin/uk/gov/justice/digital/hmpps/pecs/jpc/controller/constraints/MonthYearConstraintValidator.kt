package uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints

import uk.gov.justice.digital.hmpps.pecs.jpc.util.MonthYearParser
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class MonthYearConstraintValidator : ConstraintValidator<ValidMonthYear?, String?> {
  override fun initialize(arg0: ValidMonthYear?) {}
  override fun isValid(dateAsString: String?, context: ConstraintValidatorContext): Boolean {
    return dateAsString != null && MonthYearParser.isValid(dateAsString)
  }
}
