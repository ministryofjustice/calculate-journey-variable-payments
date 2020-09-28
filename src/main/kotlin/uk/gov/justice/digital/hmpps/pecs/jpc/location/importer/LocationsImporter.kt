package uk.gov.justice.digital.hmpps.pecs.jpc.location.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.Schedule34LocationsProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.service.Importer

@Component
class LocationsImporter(private val locationRepo: LocationRepository,
                        private val schedule34LocationsProvider: Schedule34LocationsProvider) : Importer<Unit> {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${import-files.locations}")
    private lateinit var locationsFile: String

    fun import(spreadsheet: LocationsSpreadsheet) {
        val count = locationRepo.count();

        LocationsSpreadsheet.Tab.values().forEach { tab ->
            spreadsheet.getRowsFrom(tab).forEach { row ->
                Result.runCatching { spreadsheet.mapToLocation(row).let { locationRepo.save(it) } }.onFailure { spreadsheet.addError(tab, row, it) }
            }
        }

        spreadsheet.errors.forEach { logger.info(it.toString()) }

        val inserted = locationRepo.count() - count

        logger.info("LOCATIONS INSERTED: $inserted. TOTAL ERRORS: ${spreadsheet.errors.size}")
    }

    override fun import() {
        locationRepo.deleteAll()

        schedule34LocationsProvider.get(locationsFile).use { locations ->
            LocationsSpreadsheet(XSSFWorkbook(locations)).use {
                import(it)
            }
        }
    }
}
