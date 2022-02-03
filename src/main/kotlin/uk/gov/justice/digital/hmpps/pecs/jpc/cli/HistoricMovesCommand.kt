package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.HistoricMovesProcessingService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDate

@ConditionalOnNotWebApplication
@Component
class HistoricMovesCommand(private val historicMovesProcessingService: HistoricMovesProcessingService) {
  fun process(from: LocalDate, to: LocalDate, supplier: Supplier) {
    historicMovesProcessingService.process(DateRange(from, to), supplier)
  }
}
