package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService

@ShellComponent
class BasmClientApiCommands(private val service: BasmClientApiService) {
  @ShellMethod("Retrieves the location name from the BaSM API for the supplied agency ID if there is a match.")
  fun findAgencyLocationNameFor(agencyId: String) = service.findNomisAgencyLocationNameBy(agencyId) ?: "no match"
}
