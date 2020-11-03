package uk.gov.justice.digital.hmpps.pecs.jpc.price

import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "PRICES", indexes = [Index(name = "supplier_from_to_index", columnList = "supplier, from_location_id, to_location_id", unique = true)])
data class Price(
        @Id
        @Column(name = "price_id", nullable = false)
        val id: UUID = UUID.randomUUID(),

        @Enumerated(EnumType.STRING)
        val supplier: Supplier,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "from_location_id")
        val fromLocation: Location,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "to_location_id")
        val toLocation: Location,

        val priceInPence: Int,

        @NotNull
        val addedAt: LocalDateTime = LocalDateTime.now(),
){
        fun journey() = "${fromLocation.nomisAgencyId}-${toLocation.nomisAgencyId}"

}

enum class Supplier {
        SERCO,
        GEOAMEY;

        companion object{
                fun valueOfCaseInsensitive(value: String): Supplier{
                        return valueOf(value.toUpperCase())
                }
        }
}

fun <T : Enum<*>> T.equalsStringCaseInsensitive(value: String) = this.name == value.toUpperCase()