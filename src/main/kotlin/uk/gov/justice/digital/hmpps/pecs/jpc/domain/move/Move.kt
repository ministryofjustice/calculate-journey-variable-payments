package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.EventDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.EventDateTime
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.MoveStatusParser
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.SupplierParser
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.dateConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.dateTimeConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.moveStatusConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.supplierConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
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
  @Column(name = "cancellation_reason_comment", nullable = true, length = 1024)
  var cancellationReasonComment: String? = null,

  @Json(ignored = true)
  @Column(nullable = false, length = 1024)
  var notes: String = "",

  @Json(ignored = true)
  @Transient
  val journeys: List<Journey> = listOf(),

  @Json(ignored = true)
  @Transient
  val events: List<Event> = listOf(),

  @Json(ignored = true)
  @Transient
  val person: Person? = null
) {
  fun totalInPence() =
    if (journeys.isEmpty() || journeys.count { it.priceInPence == null } > 0) null else journeys.sumOf {
      it.priceInPence ?: 0
    }

  fun hasPrice() = totalInPence() != null
  fun totalInPounds() = totalInPence()?.let { Money(it).pounds() }
  fun moveDate() = moveDate?.format(dateFormatter)
  fun pickUpDate() = pickUpDateTime?.format(dateFormatter)
  fun pickUpTime() = pickUpDateTime?.format(timeFormatter)
  fun dropOffOrCancelledDate() = dropOffOrCancelledDateTime?.format(dateFormatter)
  fun dropOffOrCancelledTime() = dropOffOrCancelledDateTime?.format(timeFormatter)
  fun fromSiteName() = fromSiteName ?: fromNomisAgencyId
  fun fromLocationType() = fromLocationType?.name ?: "NOT MAPPED"
  fun toSiteName() = toSiteName ?: toNomisAgencyId
  fun toLocationType() = toLocationType?.name ?: "NOT MAPPED"
  fun registration() = journeys.map { it.vehicleRegistrations() }.distinct().joinToString(separator = ", ")

  companion object {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val CANCELLATION_REASON_CANCELLED_BY_PMU = "cancelled_by_pmu"

    fun fromJson(json: String): Move? {
      return Klaxon().fieldConverter(MoveStatusParser::class, moveStatusConverter)
        .fieldConverter(SupplierParser::class, supplierConverter).fieldConverter(EventDate::class, dateConverter)
        .fieldConverter(EventDateTime::class, dateTimeConverter).parse<Move>(json)
    }
  }

  fun toJson(): String {
    return Klaxon().fieldConverter(EventDate::class, dateConverter)
      .fieldConverter(EventDateTime::class, dateTimeConverter).toJsonString(this)
  }

  override fun toString(): String {
    return "Move(moveId='$moveId', profileId=$profileId, updatedAt=$updatedAt, supplier=$supplier, moveType=$moveType, status=$status, reference='$reference', moveDate=$moveDate, fromNomisAgencyId='$fromNomisAgencyId', reportFromLocationType='$reportFromLocationType', fromSiteName=$fromSiteName, fromLocationType=$fromLocationType, toNomisAgencyId=$toNomisAgencyId, reportToLocationType=$reportToLocationType, toSiteName=$toSiteName, toLocationType=$toLocationType, pickUpDateTime=$pickUpDateTime, dropOffOrCancelledDateTime=$dropOffOrCancelledDateTime, cancellationReason=$cancellationReason, cancellationReasonComment=$cancellationReasonComment, notes='$notes')"
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
    return result
  }

  /**
   * Returns the nullable MoveType for the Report
   * This goes through each filterer in turn to see if it is that MoveType
   * If it doesn't find any matching MoveType, return null
   */
  fun moveType(): MoveType? {
    return MoveType.values().firstOrNull { it.hasMoveType(this) }
  }

  fun hasAllOf(vararg ets: EventType) = getEvents(*ets).size == ets.size
  fun hasAnyOf(vararg ets: EventType) = getEvents(*ets).isNotEmpty()
  fun hasNoneOf(vararg ets: EventType) = !hasAnyOf(*ets)

  fun getEvents(vararg ets: EventType) =
    this.events.filter { ets.map { it.value }.contains(it.type) } +
      this.journeys.flatMap { it.events ?: emptyList() }.filter { ets.map { it.value }.contains(it.type) }
}

enum class MoveStatus {
  proposed,
  requested,
  booked,
  in_transit,
  completed,
  cancelled,
  unknown;

  companion object {
    fun valueOfCaseInsensitive(value: String?) =
      kotlin.runCatching { valueOf(value!!.lowercase()) }.getOrDefault(unknown)
  }
}
