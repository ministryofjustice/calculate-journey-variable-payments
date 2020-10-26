package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "MOVES")
data class MoveModel(
        @Id
        @Column(name = "move_id")
        val moveId: String,

        @Enumerated(EnumType.STRING)
        val supplier: Supplier,

        @Enumerated(EnumType.STRING)
        val movePriceType: MovePriceType,

        @Enumerated(EnumType.STRING)
        val status: MoveStatus,

        @Column(name = "reference", nullable = false)
        val reference: String,

        @Column(name = "move_date", nullable = true)
        val moveDate: LocalDate? = null,

        @Column(name = "from_nomis_agency_id", nullable = false)
        val fromNomisAgencyId: String,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "from_location_id")
        val fromLocation: Location? = null,

        @Column(name = "to_nomis_agency_id", nullable = true)
        val toNomisAgencyId: String?,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "to_location_id")
        val toLocation: Location? = null,

        @Column(name = "pick_up", nullable = true)
        val pickUp: LocalDateTime? = null,

        @Column(name = "move_completed_or_cancelled", nullable = true)
        val dropOffOrCancelled: LocalDateTime?,

        val notes: String,

        @Column(name="prison_number", nullable = true)
        val prisonNumber: String? = null,

        @Column(name = "vehicle_registration", nullable = true)
        val vehicleRegistration: String?,

        @OneToMany(
                mappedBy = "move",
                cascade = arrayOf(CascadeType.ALL),
                orphanRemoval = true
        )
        val journeys: MutableList<JourneyModel> = mutableListOf()

        ) {

        fun addJourneys(vararg journeyModels: JourneyModel) {
                journeys += journeyModels
        }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as MoveModel

                if (moveId != other.moveId) return false
                if (supplier != other.supplier) return false
                if (movePriceType != other.movePriceType) return false
                if (status != other.status) return false
                if (reference != other.reference) return false
                if (moveDate != other.moveDate) return false
                if (fromNomisAgencyId != other.fromNomisAgencyId) return false
                if (toNomisAgencyId != other.toNomisAgencyId) return false
                if (pickUp != other.pickUp) return false
                if (dropOffOrCancelled != other.dropOffOrCancelled) return false
                if (notes != other.notes) return false
                if (prisonNumber != other.prisonNumber) return false
                if (vehicleRegistration != other.vehicleRegistration) return false

                return true
        }

        override fun hashCode(): Int {
                var result = moveId.hashCode()
                result = 31 * result + supplier.hashCode()
                result = 31 * result + movePriceType.hashCode()
                result = 31 * result + status.hashCode()
                result = 31 * result + reference.hashCode()
                result = 31 * result + (moveDate?.hashCode() ?: 0)
                result = 31 * result + fromNomisAgencyId.hashCode()
                result = 31 * result + toNomisAgencyId.hashCode()
                result = 31 * result + (pickUp?.hashCode() ?: 0)
                result = 31 * result + dropOffOrCancelled.hashCode()
                result = 31 * result + notes.hashCode()
                result = 31 * result + (prisonNumber?.hashCode() ?: 0)
                result = 31 * result + vehicleRegistration.hashCode()
                return result
        }

        override fun toString(): String {
                return "MoveModel(moveId=$moveId, supplier=$supplier, movePriceType=$movePriceType, status=$status, reference='$reference', moveDate=$moveDate, fromNomisAgencyId='$fromNomisAgencyId', fromLocation=$fromLocation, toNomisAgencyId='$toNomisAgencyId', toLocation=$toLocation, moveStarted=$pickUp, moveCompletedOrCancelled=$dropOffOrCancelled, notes='$notes', prisonNumber=$prisonNumber, report='$vehicleRegistration')"
        }


}