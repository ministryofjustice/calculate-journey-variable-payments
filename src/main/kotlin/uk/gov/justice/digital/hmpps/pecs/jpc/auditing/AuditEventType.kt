package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

enum class AuditEventType(val label: String) {
  LOG_IN("Log in"),
  LOG_OUT("Log out"),
  DOWNLOAD_SPREADSHEET("Download spreadsheet"),
  LOCATION_NAME_SET("Location name set"),
  LOCATION_NAME_CHANGE("Location name change"),
  LOCATION_TYPE_SET("Location type set"),
  LOCATION_TYPE_CHANGE("Location type change"),
  JOURNEY_PRICE_SET("Journey price set"),
  JOURNEY_PRICE_CHANGE("Journey price change"),
  JOURNEY_PRICE_BULK_UPDATE("Journey price bulk update");

  companion object {
    /**
     * Attempts to map the supplied value to the supported locations types.  Returns null if no match found.
     */
    fun map(value: String): AuditEventType? =
      values().firstOrNull { it.label.toUpperCase() == value.toUpperCase().trim() }
  }
}
