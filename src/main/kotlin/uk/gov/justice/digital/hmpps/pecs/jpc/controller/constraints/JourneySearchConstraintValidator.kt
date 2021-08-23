package uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints

import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class JourneySearchConstraintValidator : ConstraintValidator<ValidJourneySearch, HtmlController.SearchJourneyForm> {
  override fun initialize(arg0: ValidJourneySearch) {}

  override fun isValid(form: HtmlController.SearchJourneyForm, context: ConstraintValidatorContext) =
    !form.from.isNullOrBlank() || !form.to.isNullOrBlank()
}
