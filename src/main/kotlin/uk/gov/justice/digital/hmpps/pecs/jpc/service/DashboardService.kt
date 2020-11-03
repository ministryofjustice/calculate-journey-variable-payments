package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveTypeWithMovesAndSummary
import java.time.LocalDate

@Service
class DashboardService(private val moveQueryRepository: MoveQueryRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun movesForMonth(supplier: Supplier, date: LocalDate): MoveTypeWithMovesAndSummary{
        val filter = FilterParams(supplier, date, date.plusMonths(1).minusDays(1))
        return moveQueryRepository.findSummaryForSupplierInDateRange(filter.supplier, filter.movesFrom, filter.movesTo)
    }
}
