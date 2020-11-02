package uk.gov.justice.digital.hmpps.pecs.jpc.calculator

import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Report
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.ReportFilterer

enum class MovePriceType(val filterer: (p: FilterParams, m: Collection<Report>) -> Sequence<Report>){
    STANDARD(ReportFilterer::standardMoveReports),
    LONG_HAUL(ReportFilterer::longHaulReports),
    REDIRECTION(ReportFilterer::redirectionReports),
    LOCKOUT(ReportFilterer::lockoutReports),
    MULTI(ReportFilterer::multiTypeReports),
    CANCELLED(ReportFilterer::cancelledBillableMoves);

    companion object{
        fun valueOfCaseInsensitive(value: String): MovePriceType {
            return MovePriceType.valueOf(value.toUpperCase())
        }
    }
}

