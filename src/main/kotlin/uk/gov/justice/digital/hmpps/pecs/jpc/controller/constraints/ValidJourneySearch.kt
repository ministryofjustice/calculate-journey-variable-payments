package uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints

import jakarta.validation.Constraint
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [JourneySearchConstraintValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidJourneySearch(
  val message: String = "Please choose a pick up or drop off location",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = []
)
