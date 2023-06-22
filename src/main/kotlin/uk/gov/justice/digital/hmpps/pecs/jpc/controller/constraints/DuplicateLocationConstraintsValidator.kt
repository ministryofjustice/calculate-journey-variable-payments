package uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MapFriendlyLocationController.MapLocationForm
import uk.gov.justice.digital.hmpps.pecs.jpc.service.locations.LocationsService
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

@Component
class DuplicateLocationConstraintsValidator(
  val service: LocationsService,
) : ConstraintValidator<ValidDuplicateLocation?, MapLocationForm?> {

  override fun initialize(arg0: ValidDuplicateLocation?) {}

  override fun isValid(form: MapLocationForm?, context: ConstraintValidatorContext): Boolean {
    return form == null || form.locationName.isBlank() || !service.locationAlreadyExists(
      form.agencyId,
      form.locationName,
    )
  }
}
