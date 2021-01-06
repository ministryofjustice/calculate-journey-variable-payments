package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PricesSpreadsheetGenerator
import java.io.File
import java.time.LocalDate

@Service
class SpreadsheetService(private val pricesSpreadsheetGenerator: PricesSpreadsheetGenerator) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun spreadsheet(supplierName: String, startDate: LocalDate): File? {

    logger.info("Generating spreadsheet for supplier '$supplierName', moves from '$startDate''")

    return pricesSpreadsheetGenerator.generate(Supplier.valueOfCaseInsensitive(supplierName), startDate)
  }
}
