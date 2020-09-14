package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

fun getReportLines(path: String): List<String> {
    return object {}.javaClass.getResource(path).readText().split("\n")
}

