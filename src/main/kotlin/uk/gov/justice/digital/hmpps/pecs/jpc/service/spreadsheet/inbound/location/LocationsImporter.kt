package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.location

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.Schedule34LocationsProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository

@Component
class LocationsImporter(
  private val locationRepo: LocationRepository,
  private val priceRepo: PriceRepository,
  private val schedule34LocationsProvider: Schedule34LocationsProvider
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun import() {
    priceRepo.deleteAll()
    locationRepo.deleteAll()

    schedule34LocationsProvider.get().use { locations ->
      LocationsSpreadsheet(XSSFWorkbook(locations), locationRepo).use {
        import(it)
      }
    }
  }

  private fun import(spreadsheet: LocationsSpreadsheet) {
    val count = locationRepo.count()

    LocationsSpreadsheet.Tab.values().forEach { tab -> spreadsheet.forEachRowOn(tab) { locationRepo.save(it) } }

    spreadsheet.errors.forEach { logger.info(it.toString()) }

    val inserted = locationRepo.count() - count

    logger.info("LOCATIONS INSERTED: $inserted. TOTAL ERRORS: ${spreadsheet.errors.size}")
  }
}
