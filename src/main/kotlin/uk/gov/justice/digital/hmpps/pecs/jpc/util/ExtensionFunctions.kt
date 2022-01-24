package uk.gov.justice.digital.hmpps.pecs.jpc.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Removes the need for boilerplate code with respect to the Logger's.
 */
inline fun <reified T> loggerFor(): Logger = LoggerFactory.getLogger(T::class.java)
