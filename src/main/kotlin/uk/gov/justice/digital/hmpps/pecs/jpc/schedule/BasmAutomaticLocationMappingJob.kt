package uk.gov.justice.digital.hmpps.pecs.jpc.schedule

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BasmAutomaticLocationMappingService

@ConditionalOnWebApplication
@Component
internal class BasmAutomaticLocationMappingJob(val service: BasmAutomaticLocationMappingService) {
  fun automaticBasmLocationMapping() {
    TODO()
  }
}
