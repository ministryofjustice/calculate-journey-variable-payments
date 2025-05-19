package uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MapFriendlyLocationController.MapLocationForm
import uk.gov.justice.digital.hmpps.pecs.jpc.service.locations.LocationsService

@Component
class DuplicateLocationConstraintsValidator(
  val service: LocationsService,
) : ConstraintValidator<ValidDuplicateLocation?, MapLocationForm?> {

  override fun initialize(arg0: ValidDuplicateLocation?) {}

  override fun isValid(form: MapLocationForm?, context: ConstraintValidatorContext): Boolean = form == null ||
    form.locationName.isBlank() ||
    !service.locationAlreadyExists(
      form.agencyId,
      form.locationName,
    )
}
