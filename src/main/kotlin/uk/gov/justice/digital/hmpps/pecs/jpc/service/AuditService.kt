package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventExtra
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventExtraRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventExtraType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource

@Service
@Transactional
class AuditService(private val auditEventRepository: AuditEventRepository) {
  @Autowired
  private lateinit var timeSource: TimeSource

  @Autowired
  private lateinit var auditEventExtraRepository: AuditEventExtraRepository

  fun createLogInEvent(username: String) {
    createEvent(AuditEventType.LOG_IN, username)
  }

  fun createLogOutEvent(username: String) {
    createEvent(AuditEventType.LOG_OUT, username)
  }

  fun createDownloadSpreadsheetEvent(username: String, date: String, supplier: String) {
    val event = createEvent(AuditEventType.DOWNLOAD_SPREADSHEET, username)
    createExtra(event, "month", date)
    createExtra(event, "supplier", supplier)
  }

  private fun createExtra(event: AuditEvent, name: String, value: String) {
    auditEventExtraRepository.save(AuditEventExtra(event.id, name, value, AuditEventExtraType.STRING))
  }

  private fun createEvent(type: AuditEventType, username: String): AuditEvent {
    val event = AuditEvent(type, timeSource.dateTime(), username)
    auditEventRepository.save(event)

    return event
  }
}
