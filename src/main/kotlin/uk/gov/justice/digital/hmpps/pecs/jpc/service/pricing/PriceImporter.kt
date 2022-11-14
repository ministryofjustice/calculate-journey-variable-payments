package uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.GeoameyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditableEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
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

  enum class Action {
    WARN,
    ERROR
  }

  fun import(supplier: Supplier, effectiveYear: Int, action: Action? = Action.ERROR) {
    val start = System.currentTimeMillis()

    when (supplier) {
      Supplier.SERCO -> sercoPrices.get().use { import(it, Supplier.SERCO, effectiveYear, action) }
      Supplier.GEOAMEY -> geoameyPrices.get().use { import(it, Supplier.GEOAMEY, effectiveYear, action) }
      else -> throw RuntimeException("Supplier '$supplier' not supported.")
    }

    logger.info("Supplier $supplier prices import finished in '${(System.currentTimeMillis() - start) / 1000}' seconds.")
  }

  private fun import(prices: InputStream, supplier: Supplier, effectiveYear: Int, action: Action?) {
    logger.info("Importing prices for $supplier and effective year $effectiveYear")

    PricesSpreadsheet(
      XSSFWorkbook(prices),
      supplier,
      locationRepository.findAll(),
      priceRepo,
      effectiveYear,
      action
    ).use { import(it) }
  }

  private fun import(spreadsheet: PricesSpreadsheet) {
    val count = priceRepo.count()
    var updateCount = 0

    spreadsheet.forEachRow {

      if (it.previousPrice != null) {
        logger.info("Updating price")
        auditService.create(AuditableEvent.updatePrice(priceRepo.save(it), Money(it.previousPrice!!)))
        updateCount++
      } else {
        logger.info("Adding new price")
        auditService.create(AuditableEvent.addPrice(priceRepo.save(it)))
      }
    }

    spreadsheet.errors.forEach { logger.info(it.toString()) }

    val inserted = priceRepo.count() - count

    logger.info("${spreadsheet.supplier} PRICES INSERTED: $inserted. PRICES UPDATE: $updateCount. TOTAL ERRORS: ${spreadsheet.errors.size}")
  }
}
