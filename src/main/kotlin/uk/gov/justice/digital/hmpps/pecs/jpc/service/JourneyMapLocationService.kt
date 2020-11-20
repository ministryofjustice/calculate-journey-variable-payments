package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyRepository

@Service
class JourneyMapLocationService(private val journeyRepo: JourneyRepository) {
  fun findPickUpAndDropOffAgenciesForJourney(journeyId: String): Pair<String, String?>? =
          journeyRepo.findById(journeyId).map { Pair(it.fromNomisAgencyId, it.toNomisAgencyId) }.orElseGet { null }
}
