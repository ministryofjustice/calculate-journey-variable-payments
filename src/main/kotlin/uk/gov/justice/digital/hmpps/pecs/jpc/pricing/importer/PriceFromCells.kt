package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import org.apache.poi.ss.usermodel.Cell
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier

@Component
class PriceFromCells(private val locationRepo: LocationRepository) {

    val sheetIndex = 0

    fun getPriceResult(supplier: Supplier, cells: List<Cell>): Price {
        val journeyId = cells[0].numericCellValue
        val fromLocationName = cells[1].stringCellValue.toUpperCase().trim()
        val toLocationName = cells[2].stringCellValue.toUpperCase().trim()
        val price = cells[3].numericCellValue

        val fromLocation = locationRepo.findBySiteName(fromLocationName)
                ?: throw RuntimeException("Missing from location $fromLocationName for supplier $supplier")

        val toLocation = locationRepo.findBySiteName(toLocationName)
                ?: throw RuntimeException("Missing to location $toLocationName for supplier $supplier")

        return Price(
                supplier = supplier,
                fromLocationName = fromLocationName,
                fromLocationId = fromLocation.id,
                toLocationName = toLocationName,
                toLocationId = toLocation.id,
                journeyId = journeyId.toInt(),
                priceInPence = (price * 100).toInt())
    }
}