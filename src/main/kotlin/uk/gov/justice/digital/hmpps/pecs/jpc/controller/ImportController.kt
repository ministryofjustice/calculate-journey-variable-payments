package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService

@RestController
class ImportController(private val importService: ImportService) {

    @PostMapping("/locations/import")
    fun locations(): String = importService.locations()

    @PostMapping("/prices/import")
    fun prices(): String = importService.prices()
}
