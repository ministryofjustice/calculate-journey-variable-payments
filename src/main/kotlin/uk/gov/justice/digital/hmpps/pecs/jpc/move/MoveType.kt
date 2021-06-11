package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.MoveFilterer

enum class MoveType(val text: String, val hasMoveType: (m: Move) -> Boolean) {
  STANDARD("Standard", MoveFilterer::isStandardMove),
  LONG_HAUL("Long haul", MoveFilterer::isLongHaulMove),
  REDIRECTION("Redirection", MoveFilterer::isRedirectionMove),
  LOCKOUT("Lockout", MoveFilterer::isLockoutMove),
  MULTI("Multi type", MoveFilterer::isMultiTypeMove),
  CANCELLED("Cancelled", MoveFilterer::isCancelledBillableMove);

  companion object {
    fun valueOfCaseInsensitive(value: String): MoveType {
      return MoveType.valueOf(value.uppercase())
    }
  }
}
