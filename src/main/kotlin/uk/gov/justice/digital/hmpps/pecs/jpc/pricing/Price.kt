package uk.gov.justice.digital.hmpps.pecs.jpc.pricing

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Enumerated
import javax.persistence.EnumType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "PRICES", indexes = [Index(name = "supplier_from_to_index", columnList = "supplier, fromLocationName, toLocationName", unique = true)])
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
        val addedAt: LocalDateTime = LocalDateTime.now(),

        @Transient
        val journey: String = "${fromLocationName}-${toLocationName}"
)

enum class Supplier {
        SERCO,
        GEOAMEY;

        fun reportingName() = name.toLowerCase()
}