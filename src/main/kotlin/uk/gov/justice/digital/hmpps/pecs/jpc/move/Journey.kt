package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.*

@Entity
@Table(name = "JOURNEYS")
data class Journey(
        @Id
        @Column(name = "journey_id")
        val journeyId: String,

        @Column(name = "updated_at")
        val updatedAt: LocalDateTime,

        @Column(name = "move_id")
        val moveId: String,

        @Enumerated(EnumType.STRING)
        val supplier: Supplier,

        @Column(name = "client_timestamp")
        val clientTimeStamp: LocalDateTime?,

        @Enumerated(EnumType.STRING)
        val state: JourneyState,

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

        @Column(name = "drop_off", nullable = true)
        val dropOffDateTime: LocalDateTime? = null,

        @Column(name = "vehicle_registration", nullable = true)
        val vehicleRegistration: String? = null,

        val billable: Boolean,

        val notes: String? = null,

        @OneToMany(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
        @JoinColumn(name="eventable_id", foreignKey = javax.persistence.ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
        val events: MutableSet<Event> = mutableSetOf(),

        @Transient
        val priceInPence: Int? = null

        ) {

        override fun toString(): String {
                return "JourneyModel(journeyId='$journeyId', state=$state, fromNomisAgencyId='$fromNomisAgencyId', fromSiteName=$fromSiteName, fromLocationType=$fromLocationType, toNomisAgencyId=$toNomisAgencyId, toSiteName=$toSiteName, toLocationType=$toLocationType, pickUp=$pickUpDateTime, dropOff=$dropOffDateTime, vehicleRegistation=$vehicleRegistration, billable=$billable, notes=$notes, priceInPence=$priceInPence)"
        }

        fun addEvents(vararg events: Event) {
                this.events += events
        }

        fun hasPrice() = priceInPence != null
        fun isBillable() = if(billable) "YES" else "NO"
        fun priceInPounds() = priceInPence?.let{it.toDouble() / 100}
        fun pickUpDate() = pickUpDateTime?.format(dateFormatter)
        fun pickUpTime() = pickUpDateTime?.format(timeFormatter)
        fun dropOffDate() = dropOffDateTime?.format(dateFormatter)
        fun dropOffOrTime() = dropOffDateTime?.format(timeFormatter)
        fun fromSiteName() = fromSiteName ?: fromNomisAgencyId
        fun fromLocationType() = fromLocationType?.name ?: "NOT MAPPED"
        fun toSiteName() = toSiteName ?: toNomisAgencyId
        fun toLocationType() = toLocationType?.name ?: "NOT MAPPED"


        companion object{
                private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        }
}