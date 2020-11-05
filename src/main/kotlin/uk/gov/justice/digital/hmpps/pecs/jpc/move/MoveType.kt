package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.FilterParams
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.Report
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.ReportFilterer

enum class MoveType(val text: String, val filterer: (p: FilterParams, m: Collection<Report>) -> Sequence<Report>){
    STANDARD("Standard", ReportFilterer::standardMoveReports),
    LONG_HAUL("Long Haul", ReportFilterer::longHaulReports),
    REDIRECTION("Redirection", ReportFilterer::redirectionReports),
    LOCKOUT("Lockout", ReportFilterer::lockoutReports),
    MULTI("Multi", ReportFilterer::multiTypeReports),
    CANCELLED("Cancelled", ReportFilterer::cancelledBillableMoves);

    companion object{
        fun valueOfCaseInsensitive(value: String): MoveType {
            return MoveType.valueOf(value.toUpperCase())
        }
    }
}
