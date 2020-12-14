package uk.gov.justice.digital.hmpps.pecs.jpc.move

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.*
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.*

@Entity
@Table(name = "JOURNEYS")
data class Journey(
        @Json(name = "id")
        @Id
        @Column(name = "journey_id")
        val journeyId: String,

        @EventDateTime
        @Json(name = "updated_at")
        @Column(name = "updated_at")
        val updatedAt: LocalDateTime,

        @Json(name = "move_id")
        @Column(name = "move_id", nullable = false)
        val moveId: String,

        @SupplierParser
        @Enumerated(EnumType.STRING)
        @Column(name = "supplier", nullable = false)
        val supplier: Supplier,

        @EventDateTime
        @Json(name = "client_timestamp")
        @Column(name = "client_timestamp")
        val clientTimeStamp: LocalDateTime?,

        @JourneyStateParser
        @Enumerated(EnumType.STRING)
        @Column(name = "state", nullable = false)
        val state: JourneyState,

        @Json(name = "from_location")
        @Column(name = "from_nomis_agency_id", nullable = false)
        val fromNomisAgencyId: String,

        @Json(ignored = true)
        @Transient
        val fromSiteName: String? = null,

        @Json(ignored = true)
        @Transient
        val fromLocationType: LocationType? = null,

        @Json(name = "to_location")
        @Column(name = "to_nomis_agency_id")
        val toNomisAgencyId: String?,

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
        @Column(name = "drop_off", nullable = true)
        val dropOffDateTime: LocalDateTime? = null,

        @Json(name = "vehicle_registration")
        @Column(name = "vehicle_registration", nullable = true)
        val vehicleRegistration: String? = null,

        val billable: Boolean,

        @Json(ignored = true)
        val notes: String? = null,

        @Json(ignored = true)
        @Transient
        val events: List<Event> = listOf(),

        @Json(ignored = true)
        @Transient
        val priceInPence: Int? = null,

        @Json(ignored = true)
        @Column(name = "effective_year", nullable = false       )
        val effectiveYear: Int? = null
        ) {


        override fun toString(): String {
                return "JourneyModel(journeyId='$journeyId', state=$state, fromNomisAgencyId='$fromNomisAgencyId', fromSiteName=$fromSiteName, fromLocationType=$fromLocationType, toNomisAgencyId=$toNomisAgencyId, toSiteName=$toSiteName, toLocationType=$toLocationType, pickUp=$pickUpDateTime, dropOff=$dropOffDateTime, vehicleRegistation=$vehicleRegistration, billable=$billable, notes=$notes, priceInPence=$priceInPence)"
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

                fun fromJson(json: String): Journey? {
                        return Klaxon().
                        fieldConverter(JourneyStateParser::class, journeyStateConverter).
                        fieldConverter(SupplierParser::class, supplierConverter).
                        fieldConverter(EventDateTime::class, dateTimeConverter).
                        parse<Journey>(json)
                }
        }

        fun stateIsAnyOf(vararg states: JourneyState) = states.contains(state)

}

enum class JourneyState() {
        proposed,
        in_progress,
        rejected,
        cancelled,
        completed,
        unknown;

        companion object{
                fun valueOfCaseInsensitive(value: String?) = kotlin.runCatching { JourneyState.valueOf(value!!.toLowerCase()) }.getOrDefault(JourneyState.unknown)
        }
}