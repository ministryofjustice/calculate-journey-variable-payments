package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportStatus
import java.io.InputStream
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
class PriceImporter(private val locationRepo: LocationRepository,
                    private val priceRepo: PriceRepository,
                    private val clock: Clock,
                    private val sercoPrices: SercoPricesProvider,
                    private val geoameyPrices: GeoamyPricesProvider) {

    @Value("\${import-files.geo-prices}")
    private lateinit var geoPricesFile: String

    @Value("\${import-files.serco-prices}")
    private lateinit var sercoPricesFile: String

    private val running = AtomicBoolean(false)

    private val logger = LoggerFactory.getLogger(javaClass)

    fun import(workbook: XSSFWorkbook, supplier: Supplier) {
        val priceFromCells = PriceFromCells(locationRepo)
        val sheet = workbook.getSheetAt(priceFromCells.sheetIndex)

        val errors: MutableList<String?> = mutableListOf()
        val rows = sheet.drop(1).filterNot { it.getCell(1)?.stringCellValue.isNullOrBlank() }

        var total = 0

        rows.forEach { r ->
            total++
            val rowCells = r.iterator().asSequence().toList()
            Result.runCatching { priceRepo.save(priceFromCells.getPriceResult(supplier, rowCells)) }
                    .onFailure {
                        errors.add(it.message + " " + it.cause.toString())
                    }
        }

        errors.filterNotNull().groupBy { it }.toSortedMap().forEach { logger.info(" ${supplier.name}:  ${it.value.size} ${it.key}") }

        logger.info("$supplier PRICES INSERTED: ${total - errors.size} out of $total.")
    }

    private fun import(prices: InputStream, supplier: Supplier) {
        XSSFWorkbook(prices).use {
            import(it, supplier)
        }
    }

    fun import(): ImportStatus {
        // Imposed a crude temporary locking solution to prevent DB clashes/conflicts.
        if (running.compareAndSet(false, true)) {
            val start = LocalDateTime.now(clock)

            logger.info("Price data import started: $start")

            try {
                priceRepo.deleteAll()

                sercoPrices.get(geoPricesFile).use { prices -> import(prices, Supplier.GEOAMEY) }
                geoameyPrices.get(sercoPricesFile).use { prices -> import(prices, Supplier.SERCO) }

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