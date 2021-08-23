package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.MoveFilterer

enum class MoveType(val label: String, val description: String, val hasMoveType: (m: Move) -> Boolean) {
  STANDARD("Standard", "includes single journeys, cross supplier and redirects before the move has started", MoveFilterer::isStandardMove),
  LONG_HAUL("Long haul", "a completed move made up of 2 or more journeys", MoveFilterer::isLongHaulMove),
  REDIRECTION("Redirection", "a redirection after the move has started", MoveFilterer::isRedirectionMove),
  LOCKOUT("Lockout", "refused admission to prison", MoveFilterer::isLockoutMove),
  MULTI("Multi type", "includes combinations of standard, long haul, redirection and lockout", MoveFilterer::isMultiTypeMove),
  CANCELLED("Cancelled", "prison to prison moves cancelled by PMU later than 3pm the day before the move date", MoveFilterer::isCancelledBillableMove);

  companion object {
    fun valueOfCaseInsensitive(value: String): MoveType {
      return MoveType.valueOf(value.uppercase())
    }
  }
}
