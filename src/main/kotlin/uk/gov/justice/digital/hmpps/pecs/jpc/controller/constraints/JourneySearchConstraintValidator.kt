package uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.ManageJourneyPriceCatalogueController

class JourneySearchConstraintValidator : ConstraintValidator<ValidJourneySearch, ManageJourneyPriceCatalogueController.SearchJourneyForm> {
  override fun initialize(arg0: ValidJourneySearch) {}

  override fun isValid(form: ManageJourneyPriceCatalogueController.SearchJourneyForm, context: ConstraintValidatorContext) = !form.from.isNullOrBlank() || !form.to.isNullOrBlank()
}
