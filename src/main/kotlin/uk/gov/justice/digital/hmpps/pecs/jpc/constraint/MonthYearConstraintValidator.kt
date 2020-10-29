package uk.gov.justice.digital.hmpps.pecs.jpc.constraint

import org.ocpsoft.prettytime.nlp.PrettyTimeParser
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext


class MonthYearValidator : ConstraintValidator<ValidMonthYear?, String?> {
    override fun initialize(arg0: ValidMonthYear?) {}
    override fun isValid(date: String?, context: ConstraintValidatorContext): Boolean {
        val dates = PrettyTimeParser().parse(date)
        if (dates.size != 1) {
            return false
        }
        return true
    }
}