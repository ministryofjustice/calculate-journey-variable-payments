package uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints

import jakarta.validation.Constraint
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [DuplicateLocationConstraintsValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidDuplicateLocation(
  val message: String = "There is a problem, Schedule 34 location entered already exists, please enter a new schedule 34 location",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = []
)
