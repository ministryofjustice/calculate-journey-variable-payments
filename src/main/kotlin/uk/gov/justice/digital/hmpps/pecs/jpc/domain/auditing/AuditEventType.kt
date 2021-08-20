package uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing

enum class AuditEventType(val label: String) {
  DOWNLOAD_SPREADSHEET("Download spreadsheet"),
  DOWNLOAD_SPREADSHEET_FAILURE("Download spreadsheet failure"),
  JOURNEY_PRICE("Journey price"),
  JOURNEY_PRICE_BULK_ADJUSTMENT("Journey price bulk adjustment"),
  LOCATION("Location"),
  LOG_IN("Log in"),
  LOG_OUT("Log out"),
  REPORTING_DATA_IMPORT("Reporting data import");

  companion object {
    /**
     * Attempts to map the supplied value to the supported audit event types.  Returns null if no match found.
     */
    fun map(value: String): AuditEventType? =
      values().firstOrNull { it.label.uppercase() == value.uppercase().trim() }
  }
}
