package uk.gov.justice.digital.hmpps.pecs.jpc.constraint

import org.ocpsoft.prettytime.nlp.PrettyTimeParser
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext


class MonthYearValidator : ConstraintValidator<ValidMonthYear?, String?> {
    override fun initialize(arg0: ValidMonthYear?) {}
    override fun isValid(dateAsString: String?, context: ConstraintValidatorContext): Boolean {
        return Result.runCatching { PrettyTimeParser().parse(dateAsString).size == 1 }.getOrElse { false }
    }
}