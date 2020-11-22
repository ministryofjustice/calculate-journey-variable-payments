package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Report
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportFilterer

enum class MoveType(val text: String, val filterer: (m: Collection<Report>) -> Sequence<Report>){
    STANDARD("Standard", ReportFilterer::standardMoveReports),
    LONG_HAUL("Long haul", ReportFilterer::longHaulReports),
    REDIRECTION("Redirection", ReportFilterer::redirectionReports),
    LOCKOUT("Lockout", ReportFilterer::lockoutReports),
    MULTI("Multi type", ReportFilterer::multiTypeReports),
    CANCELLED("Cancelled", ReportFilterer::cancelledBillableMoves);

    companion object{
        fun valueOfCaseInsensitive(value: String): MoveType {
            return MoveType.valueOf(value.toUpperCase())
        }
    }
}

