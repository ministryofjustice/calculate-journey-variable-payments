package uk.gov.justice.digital.hmpps.pecs.jpc.move

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.*
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.ConstraintMode
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Transient

@Entity
@Table(name = "MOVES")
data class Move(

        @Json(name = "id")
        @Id
        @Column(name = "move_id")
        val moveId: String,

        @Json(name = "profile_id")
        @Column(name = "profile_id")
        val profileId: String? = null,

        @EventDateTime
        @Json(name = "updated_at")
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime,

        @SupplierParser
        @Enumerated(EnumType.STRING)
        @Column(name = "supplier", nullable = false)
        val supplier: Supplier,

        @Json(ignored = true)
        @Enumerated(EnumType.STRING)
        @Column(name = "move_type")
        val moveType: MoveType? = null,

        @MoveStatusParser
        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        val status: MoveStatus,

        @Column(name = "reference", nullable = false)
        val reference: String,

        @EventDate
        @Json(name = "date")
        @Column(name = "move_date")
        val moveDate: LocalDate? = null,

        @Json(name = "from_location")
        @Column(name = "from_nomis_agency_id", nullable = false)
        val fromNomisAgencyId: String,

        @Json(name = "from_location_type")
        @Column(name = "report_from_location_type", nullable = false)
        val reportFromLocationType: String,

        @Json(ignored = true)
        @Transient
        val fromSiteName: String? = null,

        @Json(ignored = true)
        @Transient
        val fromLocationType: LocationType? = null,

        @Json(name = "to_location")
        @Column(name = "to_nomis_agency_id")
        val toNomisAgencyId: String? = null,

        @Json(name = "to_location_type")
        @Column(name = "report_to_location_type")
        val reportToLocationType: String? = null,

        @Json(ignored = true)
        @Transient
        val toSiteName: String? = null,

        @Json(ignored = true)
        @Transient
        val toLocationType: LocationType? = null,

        @Json(ignored = true)
        @Column(name = "pick_up")
        val pickUpDateTime: LocalDateTime? = null,

        @Json(ignored = true)
        @Column(name = "drop_off_or_cancelled")
        val dropOffOrCancelledDateTime: LocalDateTime? = null,

        @Json(name = "cancellation_reason")
        @Column(name = "cancellation_reason")
        val cancellationReason: String? = null,

        @Json(name = "cancellation_reason_comment")
        @Column(name = "cancellation_reason_comment")
        val cancellationReasonComment: String? = null,

        @Json(ignored = true)
        val notes: String = "",

        @Json(ignored = true)
        @Column(name="prison_number")
        val prisonNumber: String? = null,

        @Json(ignored = true)
        @Column(name="latest_nomis_booking_id")
        val latestNomisBookingId: Int? = null,

        @Json(ignored = true)
        @Column(name = "first_names")
        val firstNames: String? = null,

        @Json(ignored = true)
        @Column(name = "last_name")
        val lastName: String? = null,

        @Json(ignored = true)
        @Column(name = "date_of_birth")
        val dateOfBirth: LocalDate? = null,

        @Json(ignored = true)
        @Column(name = "gender")
        val gender: String? = null,

        @Json(ignored = true)
        @Column(name = "ethnicity")
        val ethnicity: String? = null,

        @Json(ignored = true)
        @Column(name = "vehicle_registration")
        val vehicleRegistration: String? = null,

        @Json(ignored = true)
        @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
        @JoinColumn(name="move_id")
        val journeys: MutableSet<Journey> = mutableSetOf(),

        @Json(ignored = true)
        @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
        @JoinColumn(name="eventable_id", foreignKey = javax.persistence.ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
        val events: MutableSet<Event> = mutableSetOf()
)
{

        fun addJourneys(vararg journeys: Journey) {
                this.journeys += journeys
        }

        fun addEvents(vararg events: Event) {
                this.events += events
        }

        fun totalInPence() = if(journeys.isEmpty() || journeys.count { it.priceInPence == null } > 0) null else journeys.sumBy { it.priceInPence ?: 0 }

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

                val CANCELLATION_REASON_CANCELLED_BY_PMU = "cancelled_by_pmu"

                fun fromJson(json: String): Move? {
                        return Klaxon().
                        fieldConverter(MoveStatusParser::class, moveStatusConverter).
                        fieldConverter(SupplierParser::class, supplierConverter).
                        fieldConverter(EventDate::class, dateConverter).
                        fieldConverter(EventDateTime::class, dateTimeConverter).
                        parse<Move>(json)
                }
        }

        fun toJson() : String{
                return Klaxon().
                fieldConverter(EventDate::class, dateConverter).
                fieldConverter(EventDateTime::class, dateTimeConverter).
                toJsonString(this)
        }

        override fun toString(): String {
                return "Move(moveId='$moveId', profileId=$profileId, updatedAt=$updatedAt, supplier=$supplier, moveType=$moveType, status=$status, reference='$reference', moveDate=$moveDate, fromNomisAgencyId='$fromNomisAgencyId', reportFromLocationType='$reportFromLocationType', fromSiteName=$fromSiteName, fromLocationType=$fromLocationType, toNomisAgencyId=$toNomisAgencyId, reportToLocationType=$reportToLocationType, toSiteName=$toSiteName, toLocationType=$toLocationType, pickUpDateTime=$pickUpDateTime, dropOffOrCancelledDateTime=$dropOffOrCancelledDateTime, cancellationReason=$cancellationReason, cancellationReasonComment=$cancellationReasonComment, notes='$notes', prisonNumber=$prisonNumber, latestNomisBookingId=$latestNomisBookingId, firstNames=$firstNames, lastName=$lastName, dateOfBirth=$dateOfBirth, gender=$gender, ethnicity=$ethnicity, vehicleRegistration=$vehicleRegistration)"
        }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Move

                if (moveId != other.moveId) return false
                if (profileId != other.profileId) return false
                if (updatedAt != other.updatedAt) return false
                if (supplier != other.supplier) return false
                if (moveType != other.moveType) return false
                if (status != other.status) return false
                if (reference != other.reference) return false
                if (moveDate != other.moveDate) return false
                if (fromNomisAgencyId != other.fromNomisAgencyId) return false
                if (reportFromLocationType != other.reportFromLocationType) return false
                if (toNomisAgencyId != other.toNomisAgencyId) return false
                if (reportToLocationType != other.reportToLocationType) return false
                if (pickUpDateTime != other.pickUpDateTime) return false
                if (dropOffOrCancelledDateTime != other.dropOffOrCancelledDateTime) return false
                if (cancellationReason != other.cancellationReason) return false
                if (cancellationReasonComment != other.cancellationReasonComment) return false
                if (notes != other.notes) return false
                if (prisonNumber != other.prisonNumber) return false
                if (latestNomisBookingId != other.latestNomisBookingId) return false
                if (firstNames != other.firstNames) return false
                if (lastName != other.lastName) return false
                if (dateOfBirth != other.dateOfBirth) return false
                if (gender != other.gender) return false
                if (ethnicity != other.ethnicity) return false
                if (vehicleRegistration != other.vehicleRegistration) return false

                return true
        }

        override fun hashCode(): Int {
                var result = moveId.hashCode()
                result = 31 * result + (profileId?.hashCode() ?: 0)
                result = 31 * result + updatedAt.hashCode()
                result = 31 * result + supplier.hashCode()
                result = 31 * result + (moveType?.hashCode() ?: 0)
                result = 31 * result + status.hashCode()
                result = 31 * result + reference.hashCode()
                result = 31 * result + (moveDate?.hashCode() ?: 0)
                result = 31 * result + fromNomisAgencyId.hashCode()
                result = 31 * result + reportFromLocationType.hashCode()
                result = 31 * result + (toNomisAgencyId?.hashCode() ?: 0)
                result = 31 * result + (reportToLocationType?.hashCode() ?: 0)
                result = 31 * result + (pickUpDateTime?.hashCode() ?: 0)
                result = 31 * result + (dropOffOrCancelledDateTime?.hashCode() ?: 0)
                result = 31 * result + (cancellationReason?.hashCode() ?: 0)
                result = 31 * result + (cancellationReasonComment?.hashCode() ?: 0)
                result = 31 * result + notes.hashCode()
                result = 31 * result + (prisonNumber?.hashCode() ?: 0)
                result = 31 * result + (latestNomisBookingId ?: 0)
                result = 31 * result + (firstNames?.hashCode() ?: 0)
                result = 31 * result + (lastName?.hashCode() ?: 0)
                result = 31 * result + (dateOfBirth?.hashCode() ?: 0)
                result = 31 * result + (gender?.hashCode() ?: 0)
                result = 31 * result + (ethnicity?.hashCode() ?: 0)
                result = 31 * result + (vehicleRegistration?.hashCode() ?: 0)
                return result
        }
}

enum class MoveStatus {
        proposed,
        requested,
        booked,
        in_transit,
        completed,
        cancelled,
        unknown;

        companion object{
                fun valueOfCaseInsensitive(value: String?) = kotlin.runCatching { valueOf(value!!.toLowerCase())}.getOrDefault(unknown)
        }
}