package uk.gov.justice.digital.hmpps.pecs.jpc.service

import io.sentry.Sentry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Simple service for capturing free text messages in the underlying monitoring tool.
 */
@Service
class MonitoringService {

  private val logger = LoggerFactory.getLogger(javaClass)

  internal fun capture(message: String) {
    if (Sentry.isEnabled()) Sentry.captureMessage(message) else logger.warn("Monitoring is disabled, ignoring message $message.")
  }
}
