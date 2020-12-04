package uk.gov.justice.digital.hmpps.pecs.jpc.constraint

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MapFriendlyLocationController.MapLocationForm
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MapFriendlyLocationService
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

@Component
class DuplicateLocationConstraintsValidator(val service: MapFriendlyLocationService) : ConstraintValidator<ValidDuplicateLocation?, MapLocationForm?> {

  override fun initialize(arg0: ValidDuplicateLocation?) {}

  override fun isValid(form: MapLocationForm?, context: ConstraintValidatorContext): Boolean {
    return form == null || form.locationName.isEmpty() || !service.locationAlreadyExists(form.agencyId, form.locationName)
  }
}
