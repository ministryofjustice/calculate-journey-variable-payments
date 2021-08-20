package uk.gov.justice.digital.hmpps.pecs.jpc.domain.location

/**
 * Enum responsible for mapping location type string values in the locations spreadsheet to an abbreviation (in this case the enum value).
 */
enum class LocationType(val label: String) {

  AP("Airport"),
  APP("Approved Premises"),
  CC("Crown Court"),
  CM("Combined Court"),
  CO("County Court"),
  CRN("Coroners Court"),
  CRT("Court"),
  HP("Hospital"),
  IM("Immigration"),
  MC("Mag Court"),
  O("Other"),
  PB("Probation"),
  PR("Prison"),
  PS("Police"),
  SCH("SCH"),
  STC("STC");

  companion object {
    /**
     * Attempts to map the supplied value to the supported locations types.  Returns null if no match found.
     */
    fun map(value: String): LocationType? =
      values().firstOrNull { it.label.uppercase() == value.uppercase().trim() }
  }
}
