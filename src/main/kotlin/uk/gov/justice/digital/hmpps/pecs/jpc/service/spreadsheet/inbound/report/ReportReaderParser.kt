package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report

import java.io.Reader

class ReportReaderParser(private val reader: (String) -> Reader) {
  fun <T> forEach(reportName: String, parser: (String) -> T?, consumer: (T) -> Unit) {
    reader(reportName).forEachLine { parser(it)?.run(consumer) }
  }
}
