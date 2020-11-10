package uk.gov.justice.digital.hmpps.pecs.jpc.move

data class JourneysSummary(val count: Int, val totalPriceInPence: Int, val countWithoutLocations: Int, val countUnpriced: Int)