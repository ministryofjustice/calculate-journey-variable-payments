package uk.gov.justice.digital.hmpps.pecs.jpc.price

import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "PRICES", indexes = [Index(name = "supplier_from_to_year_index", columnList = "supplier, from_location_id, to_location_id, effective_year", unique = true)])
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

        var priceInPence: Int,

        @NotNull
        val addedAt: LocalDateTime = LocalDateTime.now(),

        @Column(name = "effective_year", nullable = false)
        val effectiveYear: Int
){
        fun journey() = "${fromLocation.nomisAgencyId}-${toLocation.nomisAgencyId}"

        fun price() = Money(priceInPence)
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

fun effectiveYearForDate(date: LocalDate)= if(date.monthValue >= 9) date.year else date.year -1
