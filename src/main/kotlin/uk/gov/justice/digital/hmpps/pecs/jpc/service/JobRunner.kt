package uk.gov.justice.digital.hmpps.pecs.jpc.service

fun interface JobRunner {
  fun run(label: String, job: () -> Unit)
}
