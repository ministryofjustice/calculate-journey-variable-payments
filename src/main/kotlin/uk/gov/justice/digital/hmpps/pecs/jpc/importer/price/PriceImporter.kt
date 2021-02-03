package uk.gov.justice.digital.hmpps.pecs.jpc.importer.price

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoameyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.io.InputStream

@Component
class PriceImporter(
  private val priceRepo: PriceRepository,
  private val sercoPrices: SercoPricesProvider,
  private val geoameyPrices: GeoameyPricesProvider,
  private val locationRepository: LocationRepository
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun import(supplier: Supplier) {
    val start = System.currentTimeMillis()

    logger.info("Importing prices for $supplier")

    when (supplier) {
      Supplier.SERCO -> {
        priceRepo.deleteBySupplier(Supplier.SERCO)
        sercoPrices.get().use { import(it, Supplier.SERCO) }
      }
      Supplier.GEOAMEY -> {
        priceRepo.deleteBySupplier(Supplier.GEOAMEY)
        geoameyPrices.get().use { import(it, Supplier.GEOAMEY) }
      }
      else -> throw RuntimeException("Supplier '$supplier' not supported.")
    }

    logger.info("Supplier $supplier prices import finished in '${(System.currentTimeMillis() - start) / 1000}' seconds.")
  }

  private fun import(prices: InputStream, supplier: Supplier) =
    PricesSpreadsheet(XSSFWorkbook(prices), supplier, locationRepository.findAll().toList(), priceRepo).use { import(it) }

  private fun import(spreadsheet: PricesSpreadsheet) {
    val count = priceRepo.count()

    spreadsheet.forEachRow { priceRepo.save(it) }

    spreadsheet.errors.forEach { logger.info(it.toString()) }

    val inserted = priceRepo.count() - count

    logger.info("${spreadsheet.supplier} PRICES INSERTED: $inserted. TOTAL ERRORS: ${spreadsheet.errors.size}")
  }
}
