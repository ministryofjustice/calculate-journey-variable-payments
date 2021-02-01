package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

enum class AuditEventType(val label: String) {
  LOG_IN("Log in"),
  LOG_OUT("Log out"),
  DOWNLOAD_SPREADSHEET("Download spreadsheet");

  companion object {
    /**
     * Attempts to map the supplied value to the supported locations types.  Returns null if no match found.
     */
    fun map(value: String): AuditEventType? =
      values().firstOrNull { it.label.toUpperCase() == value.toUpperCase().trim() }
  }
}
