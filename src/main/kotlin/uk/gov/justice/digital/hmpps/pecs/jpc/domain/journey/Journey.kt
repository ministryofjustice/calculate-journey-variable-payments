package uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey

import com.beust.klaxon.Converter
import com.beust.klaxon.Json
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.Transient
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.JsonDateTimeConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.JsonSupplierConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.jsonDateTimeConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.jsonSupplierConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "JOURNEYS", indexes = [Index(name = "move_id_index", columnList = "move_id", unique = false)])
data class Journey(
  @Json(name = "id")
  @Id
  @Column(name = "journey_id")
  val journeyId: String,

  @JsonDateTimeConverter
  @Json(name = "updated_at")
  @Column(name = "updated_at")
  val updatedAt: LocalDateTime,

  @Json(name = "move_id")
  @Column(name = "move_id", nullable = false)
  val moveId: String,

  @JsonSupplierConverter
  @Enumerated(EnumType.STRING)
  @Column(name = "supplier", nullable = false)
  val supplier: Supplier,

  @JsonDateTimeConverter
  @Json(name = "client_timestamp")
  @Column(name = "client_timestamp")
  val clientTimeStamp: LocalDateTime?,

  @JsonJourneyStateConverter
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
  private val vehicleRegistration: String? = null,

  val billable: Boolean,

  @Json(ignored = true)
  @Column(nullable = true, length = 1024)
  val notes: String? = null,

  @Json(ignored = true)
  @Transient
  val events: List<Event>? = null,

  @Json(ignored = true)
  @Transient
  val priceInPence: Int? = null,

  @Json(ignored = true)
  @Column(name = "effective_year", nullable = false)
  val effectiveYear: Int? = null
) {
  override fun toString(): String {
    return "JourneyModel(journeyId='$journeyId', state=$state, fromNomisAgencyId='$fromNomisAgencyId', fromSiteName=$fromSiteName, fromLocationType=$fromLocationType, toNomisAgencyId=$toNomisAgencyId, toSiteName=$toSiteName, toLocationType=$toLocationType, pickUp=$pickUpDateTime, dropOff=$dropOffDateTime, vehicleRegistation=$vehicleRegistration, billable=$billable, notes=$notes, priceInPence=$priceInPence)"
  }

  fun hasPrice() = priceInPence != null
  fun isBillable() = if (billable) "YES" else "NO"
  fun priceInPounds() = priceInPence?.let { Money(it).pounds() }
  fun pickUpDate() = pickUpDateTime?.format(dateFormatter)
  fun pickUpTime() = pickUpDateTime?.format(timeFormatter)
  fun dropOffDate() = dropOffDateTime?.format(dateFormatter)
  fun dropOffOrTime() = dropOffDateTime?.format(timeFormatter)
  fun fromSiteName() = fromSiteName ?: fromNomisAgencyId
  fun fromLocationType() = fromLocationType?.name ?: "NOT MAPPED"
  fun toSiteName() = toSiteName ?: toNomisAgencyId
  fun toLocationType() = toLocationType?.name ?: "NOT MAPPED"

  /**
   * A journey can have multiple registrations. The vehicle may change due to unforeseen circumstances e.g. breakdown.
   */
  fun vehicleRegistrations() =
    events?.startAndCompleteEvents()
      ?.vehicleRegistration()
      ?.distinct()
      ?.joinToString(separator = ", ")
      ?.ifEmpty { vehicleRegistration } ?: vehicleRegistration

  private fun List<Event>.startAndCompleteEvents() =
    this.filter { it.hasType(EventType.JOURNEY_START) || it.hasType(EventType.JOURNEY_COMPLETE) }
      .sortedBy { it.occurredAt }

  private fun List<Event>.vehicleRegistration() = this.mapNotNull { it.vehicleRegistration() }

  companion object {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun fromJson(json: String): Journey? {
      return Klaxon().fieldConverter(JsonJourneyStateConverter::class, jsonJourneyStateConverter)
        .fieldConverter(JsonSupplierConverter::class, jsonSupplierConverter)
        .fieldConverter(JsonDateTimeConverter::class, jsonDateTimeConverter).parse<Journey>(json)
    }
  }

  fun stateIsAnyOf(vararg states: JourneyState) = states.contains(state)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Journey

    if (journeyId != other.journeyId) return false
    if (updatedAt != other.updatedAt) return false
    if (moveId != other.moveId) return false
    if (supplier != other.supplier) return false
    if (clientTimeStamp != other.clientTimeStamp) return false
    if (state != other.state) return false
    if (fromNomisAgencyId != other.fromNomisAgencyId) return false
    if (fromSiteName != other.fromSiteName) return false
    if (fromLocationType != other.fromLocationType) return false
    if (toNomisAgencyId != other.toNomisAgencyId) return false
    if (toSiteName != other.toSiteName) return false
    if (toLocationType != other.toLocationType) return false
    if (pickUpDateTime != other.pickUpDateTime) return false
    if (dropOffDateTime != other.dropOffDateTime) return false
    if (vehicleRegistration != other.vehicleRegistration) return false
    if (billable != other.billable) return false
    if (notes != other.notes) return false
    if (events != other.events) return false
    if (priceInPence != other.priceInPence) return false
    if (effectiveYear != other.effectiveYear) return false

    return true
  }

  override fun hashCode(): Int {
    var result = journeyId.hashCode()
    result = 31 * result + updatedAt.hashCode()
    result = 31 * result + moveId.hashCode()
    result = 31 * result + supplier.hashCode()
    result = 31 * result + (clientTimeStamp?.hashCode() ?: 0)
    result = 31 * result + state.hashCode()
    result = 31 * result + fromNomisAgencyId.hashCode()
    result = 31 * result + (fromSiteName?.hashCode() ?: 0)
    result = 31 * result + (fromLocationType?.hashCode() ?: 0)
    result = 31 * result + (toNomisAgencyId?.hashCode() ?: 0)
    result = 31 * result + (toSiteName?.hashCode() ?: 0)
    result = 31 * result + (toLocationType?.hashCode() ?: 0)
    result = 31 * result + (pickUpDateTime?.hashCode() ?: 0)
    result = 31 * result + (dropOffDateTime?.hashCode() ?: 0)
    result = 31 * result + (vehicleRegistration?.hashCode() ?: 0)
    result = 31 * result + billable.hashCode()
    result = 31 * result + (notes?.hashCode() ?: 0)
    result = 31 * result + (events?.hashCode() ?: 0)
    result = 31 * result + (priceInPence ?: 0)
    result = 31 * result + (effectiveYear ?: 0)

    return result
  }
}

enum class JourneyState() {
  proposed,
  in_progress,
  rejected,
  cancelled,
  completed,
  unknown;

  companion object {
    fun valueOfCaseInsensitive(value: String?) =
      kotlin.runCatching { valueOf(value!!.lowercase()) }.getOrDefault(unknown)
  }
}

@Target(AnnotationTarget.FIELD)
annotation class JsonJourneyStateConverter

val jsonJourneyStateConverter = object : Converter {

  override fun canConvert(cls: Class<*>) = cls == JourneyState::class.java

  override fun fromJson(jv: JsonValue) = JourneyState.valueOfCaseInsensitive(jv.string)

  override fun toJson(value: Any) =
    """"$value""""
}
