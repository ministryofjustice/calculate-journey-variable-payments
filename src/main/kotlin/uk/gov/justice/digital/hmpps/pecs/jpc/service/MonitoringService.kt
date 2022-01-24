package uk.gov.justice.digital.hmpps.pecs.jpc.service

import io.sentry.Sentry
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

/**
 * Simple service for capturing free text messages in the underlying monitoring tool.
 *
 * Extra care must be taken when using this service not to include any PII data in any calls.
 */
private val logger = loggerFor<MonitoringService>()

@Service
class MonitoringService {

  internal fun capture(message: String) {
    if (Sentry.isEnabled()) {
      Sentry.captureMessage(message)
      logger.warn(message)
    } else logger.warn("Monitoring is disabled, ignoring message $message.")
  }
}
