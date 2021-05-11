package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AutomaticLocationMappingService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmClientApiService
import java.time.LocalDate

@ShellComponent
class BasmClientApiCommands(
  private val service: BasmClientApiService,
  private val mappingService: AutomaticLocationMappingService
) {
  @ShellMethod("Retrieves the location name from the BaSM API for the supplied agency ID if there is a match.")
  fun findAgencyLocationNameFor(agencyId: String) = service.findNomisAgencyLocationNameBy(agencyId) ?: "no match"

  @ShellMethod("Retrieves and maps new locations added/created on the given date (if any).")
  fun mapNomisLocationsOn(date: LocalDate) = mappingService.mapIfNotPresentLocationsCreatedOn(date)
}
