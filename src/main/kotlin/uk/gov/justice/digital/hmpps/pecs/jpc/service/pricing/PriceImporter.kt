package uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.GeoameyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.io.InputStream

private val logger = loggerFor<PriceImporter>()

@Component
class PriceImporter(
  private val priceRepo: PriceRepository,
  private val sercoPrices: SercoPricesProvider,
  private val geoameyPrices: GeoameyPricesProvider,
  private val locationRepository: LocationRepository,
  private val auditService: AuditService
) {

  fun import(supplier: Supplier, effectiveYear: Int) {
    val start = System.currentTimeMillis()

    when (supplier) {
      Supplier.SERCO -> sercoPrices.get().use { import(it, Supplier.SERCO, effectiveYear) }
      Supplier.GEOAMEY -> geoameyPrices.get().use { import(it, Supplier.GEOAMEY, effectiveYear) }
      else -> throw RuntimeException("Supplier '$supplier' not supported.")
    }

    logger.info("Supplier $supplier prices import finished in '${(System.currentTimeMillis() - start) / 1000}' seconds.")
  }

  private fun import(prices: InputStream, supplier: Supplier, effectiveYear: Int) {
    logger.info("Importing prices for $supplier and effective year $effectiveYear")

    PricesSpreadsheet(
      XSSFWorkbook(prices),
      supplier,
      locationRepository.findAll(),
      priceRepo,
      effectiveYear
    ).use { import(it) }
  }

  private fun import(spreadsheet: PricesSpreadsheet) {
    val count = priceRepo.count()

    spreadsheet.forEachRow { auditService.create(AuditableEvent.addPrice(priceRepo.save(it))) }

    spreadsheet.errors.forEach { logger.info(it.toString()) }

    val inserted = priceRepo.count() - count

    logger.info("${spreadsheet.supplier} PRICES INSERTED: $inserted. TOTAL ERRORS: ${spreadsheet.errors.size}")
  }
}
