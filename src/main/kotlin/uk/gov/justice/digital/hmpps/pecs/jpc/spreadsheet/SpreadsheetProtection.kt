package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import java.io.File

fun interface SpreadsheetProtection {
    fun protectAndGet(file: File): File
}