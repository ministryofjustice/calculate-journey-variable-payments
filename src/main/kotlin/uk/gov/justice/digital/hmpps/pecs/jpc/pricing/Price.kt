package uk.gov.justice.digital.hmpps.pecs.jpc.pricing

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import javax.validation.constraints.Min

@Entity
class Price(
        @Enumerated(EnumType.STRING) val supplier: Supplier,
        @Column(unique = true, nullable = false) val journeyId: Int,
        @NotNull @Size(min = 1) val fromLocationName: String,
        @NotNull val fromLocationId: UUID,
        @NotNull @Size(min = 1) val toLocationName: String,
        @NotNull val toLocationId: UUID,
        @NotNull @Min(1) val priceInPence: Int,
        @NotNull val addedAt: LocalDateTime = LocalDateTime.now(),
        @Id val id: UUID = UUID.randomUUID())

enum class Supplier {SERCO, GEOAMEY}