package uk.gov.justice.digital.hmpps.pecs.jpc.constraint

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import javax.validation.Constraint
import kotlin.reflect.KClass


@Documented
@Constraint(validatedBy = [MonthYearValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(RetentionPolicy.RUNTIME)
annotation class ValidMonthYear(val message: String = "Invalid date", val groups: Array<KClass<*>> = [], val payload: Array<KClass<out Any>> = [])