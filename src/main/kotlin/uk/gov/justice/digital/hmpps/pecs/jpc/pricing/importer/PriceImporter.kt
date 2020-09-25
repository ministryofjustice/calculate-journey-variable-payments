package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportStatus
import java.io.InputStream
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
class PriceImporter(private val priceRepo: PriceRepository,
                    private val clock: Clock,
                    private val sercoPrices: SercoPricesProvider,
                    private val geoameyPrices: GeoamyPricesProvider,
                    private val locationRepository: LocationRepository) {

    @Value("\${import-files.geo-prices}")
    private lateinit var geoPricesFile: String

    @Value("\${import-files.serco-prices}")
    private lateinit var sercoPricesFile: String

    private val running = AtomicBoolean(false)

    private val logger = LoggerFactory.getLogger(javaClass)

    internal fun import(spreadsheet: PricesSpreadsheet) {
        val count = priceRepo.count()

        spreadsheet.getRows().forEach { row ->
            Result.runCatching { spreadsheet.mapToPrice(row).let { priceRepo.save(it) } }.onFailure { spreadsheet.addError(row, it) }
        }

        spreadsheet.errors.forEach { logger.info(it.toString()) }

        val inserted = priceRepo.count() - count

        logger.info("${spreadsheet.supplier} PRICES INSERTED: $inserted. TOTAL ERRORS: ${spreadsheet.errors.size}")
    }

    private fun import(prices: InputStream, supplier: Supplier) {
        PricesSpreadsheet(XSSFWorkbook(prices), supplier, locationRepository).use {
            import(it)
        }
    }

    fun import(): ImportStatus {
        // Imposed a crude temporary locking solution to prevent DB clashes/conflicts.
        if (running.compareAndSet(false, true)) {
            val start = LocalDateTime.now(clock)

            logger.info("Price data import started: $start")

            try {
                priceRepo.deleteAll()

                sercoPrices.get(geoPricesFile).use { import(it, Supplier.GEOAMEY) }
                geoameyPrices.get(sercoPricesFile).use { import(it, Supplier.SERCO) }

                return ImportStatus.DONE
            } finally {
                running.set(false)

                val end = LocalDateTime.now(clock)

                logger.info("Price import ended: $end. Time taken (in seconds): ${Duration.between(start, end).seconds}")
            }
        }

        return ImportStatus.IN_PROGRESS
    }
}