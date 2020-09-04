package uk.gov.justice.digital.hmpps.pecs.jpc.pricing

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class Price(
        var supplier: String,
        var journeyId: Int,
        var fromLocationName: String,
        var fromLocationId: UUID,
        var toLocationName: String,
        var toLocationId: UUID,
        var priceInPence: Int,
        var addedAt: LocalDateTime = LocalDateTime.now(),
        @Id var id: UUID = UUID.randomUUID())
