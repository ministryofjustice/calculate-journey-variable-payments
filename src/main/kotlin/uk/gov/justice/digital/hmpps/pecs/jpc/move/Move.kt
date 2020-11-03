package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.*

@Entity
@Table(name = "MOVES")
data class Move(
        @Id
        @Column(name = "move_id")
        val moveId: String,

        @Enumerated(EnumType.STRING)
        val supplier: Supplier,

        @Enumerated(EnumType.STRING)
        @Column(name = "move_price_type", nullable = false)
        val moveType: MoveType,

        @Enumerated(EnumType.STRING)
        val status: MoveStatus,

        @Column(name = "reference", nullable = false)
        val reference: String,

        @Column(name = "move_date", nullable = true)
        val moveDate: LocalDate? = null,

        @Column(name = "from_nomis_agency_id", nullable = false)
        val fromNomisAgencyId: String,

        @Transient
        val fromSiteName: String? = null,

        @Transient
        val fromLocationType: LocationType? = null,

        @Column(name = "to_nomis_agency_id", nullable = true)
        val toNomisAgencyId: String?,

        @Transient
        val toSiteName: String? = null,

        @Transient
        val toLocationType: LocationType? = null,

        @Column(name = "pick_up", nullable = true)
        val pickUpDateTime: LocalDateTime? = null,

        @Column(name = "drop_off_or_cancelled", nullable = true)
        val dropOffOrCancelledDateTime: LocalDateTime?,

        val notes: String,

        @Column(name="prison_number", nullable = true)
        val prisonNumber: String? = null,

        @Column(name = "vehicle_registration", nullable = true)
        val vehicleRegistration: String?,

        @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        @JoinColumn(name="move_id")
        val journeys: MutableList<Journey> = mutableListOf()

        ) {

        fun addJourneys(vararg journeys: Journey) {
                this.journeys += journeys
        }

        fun totalInPence() = if(journeys.isEmpty() || journeys.count { it.priceInPence == null } > 0) null else journeys.sumBy { it.priceInPence ?: 0 }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Move

                if (moveId != other.moveId) return false
                if (supplier != other.supplier) return false
                if (moveType != other.moveType) return false
                if (status != other.status) return false
                if (reference != other.reference) return false
                if (moveDate != other.moveDate) return false
                if (fromNomisAgencyId != other.fromNomisAgencyId) return false
                if (toNomisAgencyId != other.toNomisAgencyId) return false
                if (pickUpDateTime != other.pickUpDateTime) return false
                if (dropOffOrCancelledDateTime != other.dropOffOrCancelledDateTime) return false
                if (notes != other.notes) return false
                if (prisonNumber != other.prisonNumber) return false
                if (vehicleRegistration != other.vehicleRegistration) return false

                return true
        }

        override fun hashCode(): Int {
                var result = moveId.hashCode()
                result = 31 * result + supplier.hashCode()
                result = 31 * result + moveType.hashCode()
                result = 31 * result + status.hashCode()
                result = 31 * result + reference.hashCode()
                result = 31 * result + (moveDate?.hashCode() ?: 0)
                result = 31 * result + fromNomisAgencyId.hashCode()
                result = 31 * result + toNomisAgencyId.hashCode()
                result = 31 * result + (pickUpDateTime?.hashCode() ?: 0)
                result = 31 * result + dropOffOrCancelledDateTime.hashCode()
                result = 31 * result + notes.hashCode()
                result = 31 * result + (prisonNumber?.hashCode() ?: 0)
                result = 31 * result + vehicleRegistration.hashCode()
                return result
        }

        override fun toString(): String {
                return "MoveModel(moveId='$moveId', supplier=$supplier, movePriceType=$moveType, status=$status, reference='$reference', moveDate=$moveDate, fromNomisAgencyId='$fromNomisAgencyId', fromSiteName=$fromSiteName, fromLocationType=$fromLocationType, toNomisAgencyId=$toNomisAgencyId, toSiteName=$toSiteName, toLocationType=$toLocationType, pickUp=$pickUpDateTime, dropOffOrCancelled=$dropOffOrCancelledDateTime, notes='$notes', prisonNumber=$prisonNumber, vehicleRegistration=$vehicleRegistration)"
        }

        fun hasPrice() = totalInPence() != null
        fun totalInPounds() = totalInPence()?.let{it.toDouble() / 100}
        fun moveDate() = moveDate?.format(dateFormatter)
        fun pickUpDate() = pickUpDateTime?.format(dateFormatter)
        fun pickUpTime() = pickUpDateTime?.format(timeFormatter)
        fun dropOffOrCancelledDate() = dropOffOrCancelledDateTime?.format(dateFormatter)
        fun dropOffOrCancelledTime() = dropOffOrCancelledDateTime?.format(timeFormatter)
        fun fromSiteName() = fromSiteName ?: fromNomisAgencyId
        fun fromLocationType() = fromLocationType?.name ?: "NOT MAPPED"
        fun toSiteName() = toSiteName ?: toNomisAgencyId
        fun toLocationType() = toLocationType?.name ?: "NOT MAPPED"




        companion object{
                private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        }
}