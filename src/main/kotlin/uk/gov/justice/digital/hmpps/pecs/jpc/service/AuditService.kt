package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.beust.klaxon.Klaxon
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money

@Service
@Transactional
class AuditService(private val auditEventRepository: AuditEventRepository) {
  @Autowired
  private lateinit var timeSource: TimeSource

  fun createLogInEvent(username: String) {
    create(
      AuditableEvent(
        AuditEventType.LOG_IN,
        username,
        timeSource.dateTime()
      )
    )
  }

  fun createLogOutEvent(username: String) {
    create(
      AuditableEvent(
        AuditEventType.LOG_OUT,
        username,
        timeSource.dateTime()
      )
    )
  }

  fun createDownloadSpreadsheetEvent(username: String, date: String, supplier: String) {
    create(
      AuditableEvent(
        AuditEventType.DOWNLOAD_SPREADSHEET,
        username,
        timeSource.dateTime(),
        mapOf("month" to date, "supplier" to supplier)
      )
    )
  }

  fun createLocationNameSetEvent(username: String, nomisId: String, name: String) {
    create(
      AuditableEvent(
        AuditEventType.LOCATION_NAME_SET,
        username,
        timeSource.dateTime(),
        mapOf("nomisId" to nomisId, "name" to name)
      )
    )
  }

  fun createLocationNameChangeEvent(username: String, nomisId: String, oldName: String, newName: String) {
    create(
      AuditableEvent(
        AuditEventType.LOCATION_NAME_CHANGE,
        username,
        timeSource.dateTime(),
        mapOf("nomisId" to nomisId, "oldName" to oldName, "newName" to newName)
      )
    )
  }

  fun createLocationTypeSetEvent(username: String, nomisId: String, type: LocationType) {
    create(
      AuditableEvent(
        AuditEventType.LOCATION_TYPE_SET,
        username,
        timeSource.dateTime(),
        mapOf("nomisId" to nomisId, "type" to type)
      )
    )
  }

  fun createLocationTypeChangeEvent(username: String, nomisId: String, oldType: LocationType, newType: LocationType) {
    create(
      AuditableEvent(
        AuditEventType.LOCATION_TYPE_CHANGE,
        username,
        timeSource.dateTime(),
        mapOf("nomisId" to nomisId, "oldType" to oldType, "newType" to newType)
      )
    )
  }

  fun createJourneyPriceSetEvent(
    username: String,
    supplier: String,
    fromNomisId: String,
    toNomisId: String,
    price: Money
  ) {
    create(
      AuditableEvent(
        AuditEventType.JOURNEY_PRICE_SET,
        username,
        timeSource.dateTime(),
        mapOf("supplier" to supplier, "fromNomisId" to fromNomisId, "toNomisId" to toNomisId, "price" to price.pounds())
      )
    )
  }

  fun createJourneyPriceChangeEvent(
    username: String,
    supplier: String,
    fromNomisId: String,
    toNomisId: String,
    oldPrice: Money,
    newPrice: Money
  ) {
    create(
      AuditableEvent(
        AuditEventType.JOURNEY_PRICE_CHANGE,
        username,
        timeSource.dateTime(),
        mapOf(
          "supplier" to supplier,
          "fromNomisId" to fromNomisId,
          "toNomisId" to toNomisId,
          "oldPrice" to oldPrice.pounds(),
          "newPrice" to newPrice.pounds()
        )
      )
    )
  }

  fun createJourneyPriceBulkUpdateEvent(supplier: String, multiplier: Double) {
    create(
      AuditableEvent(
        AuditEventType.JOURNEY_PRICE_BULK_UPDATE,
        "_TERMINAL_",
        timeSource.dateTime(),
        mapOf("supplier" to supplier, "multiplier" to multiplier)
      )
    )
  }

  private fun create(event: AuditableEvent) {
    auditEventRepository.save(
      AuditEvent(
        event.type,
        event.timestamp,
        event.username.trim().toUpperCase(),
        Klaxon().toJsonString(event.extras)
      )
    )
  }
}
