package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

enum class AuditEventExtraType(val label: String) {
  STRING("String");

  companion object {
    /**
     * Attempts to map the supplied value to the supported locations types.  Returns null if no match found.
     */
    fun map(value: String): AuditEventExtraType? =
      values().firstOrNull { it.label.toUpperCase() == value.toUpperCase().trim() }
  }
}
