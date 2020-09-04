package uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer

import org.apache.poi.ss.usermodel.Cell
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price

@Component
class ExcelTab(private val locationRepo: LocationRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)
    val sheetIndex = 0

    fun priceFromRowCells(supplier: String, cells: List<Cell>) : Price? {

        val journeyId = cells[0].numericCellValue
        val fromLocationName = cells[1].stringCellValue.toUpperCase().trim()
        val toLocationName = cells[2].stringCellValue.toUpperCase().trim()
        val price = cells[3].numericCellValue


        val fromLocation = locationRepo.findBySiteName(fromLocationName)
        val toLocation = locationRepo.findBySiteName(toLocationName)

        if(fromLocation == null){
            logger.error("ERROR importing price, cannot find from location $fromLocationName")
            return null
        }

        if(toLocation == null){
            logger.error("ERROR importing price, cannot find to location $toLocationName")
            return null
        }

        return Price(
                supplier = supplier,
                fromLocationName = fromLocationName,
                fromLocationId = fromLocation.id,
                toLocationName = toLocationName,
                toLocationId = toLocation.id,
                journeyId = journeyId.toInt(),
                priceInPence = (price * 100).toInt()
        )
    }
}