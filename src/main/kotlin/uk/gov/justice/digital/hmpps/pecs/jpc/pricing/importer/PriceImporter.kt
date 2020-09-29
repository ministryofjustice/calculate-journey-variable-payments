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
import uk.gov.justice.digital.hmpps.pecs.jpc.service.Importer
import java.io.InputStream

@Component
class PriceImporter(private val priceRepo: PriceRepository,
                    private val sercoPrices: SercoPricesProvider,
                    private val geoameyPrices: GeoamyPricesProvider,
                    private val locationRepository: LocationRepository) : Importer<Unit> {

    @Value("\${import-files.geo-prices}")
    private lateinit var geoPricesFile: String

    @Value("\${import-files.serco-prices}")
    private lateinit var sercoPricesFile: String

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
        PricesSpreadsheet(XSSFWorkbook(prices), supplier, locationRepository, priceRepo).use {
            import(it)
        }
    }

    override fun import() {
        priceRepo.deleteAll()
        sercoPrices.get(sercoPricesFile).use { import(it, Supplier.SERCO) }
        geoameyPrices.get(geoPricesFile).use { import(it, Supplier.GEOAMEY) }
    }
}