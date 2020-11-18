package uk.gov.justice.digital.hmpps.pecs.jpc.constraint

import javax.validation.Constraint
import kotlin.reflect.KClass


@MustBeDocumented
@Constraint(validatedBy = [MonthYearValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidMonthYear(val message: String = "Invalid date", val groups: Array<KClass<*>> = [], val payload: Array<KClass<out Any>> = [])