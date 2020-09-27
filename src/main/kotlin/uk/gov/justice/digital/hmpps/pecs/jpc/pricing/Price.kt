package uk.gov.justice.digital.hmpps.pecs.jpc.pricing

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "PRICES")
data class Price(
        @Id
        @Column(name = "price_id", nullable = false)
        val id: UUID = UUID.randomUUID(),

        @Enumerated(EnumType.STRING)
        val supplier: Supplier,

        @Column(unique = false, nullable = false)
        val journeyId: Int,

        @get: NotBlank(message = "Price from location name cannot be empty.")
        val fromLocationName: String,

        @get: NotNull(message = "Price from location identifier cannot be null.")
        val fromLocationId: UUID,

        @get: NotBlank(message = "Price to location name cannot be empty.")
        val toLocationName: String,

        @get: NotNull(message = "Price to location identifier cannot be null.")
        val toLocationId: UUID,

        @get: Min(1, message = "Price must be greater than zero.")
        val priceInPence: Int,

        @NotNull
        val addedAt: LocalDateTime = LocalDateTime.now()
)

enum class Supplier {
        SERCO,
        GEOAMEY;

        fun reportingName() = name.toLowerCase()
}