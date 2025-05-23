package uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.JsonDateConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.JsonDateTimeConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.jsonDateConverter
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.jsonDateTimeConverter
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "PEOPLE")
data class Person(
  @Json(name = "id")
  @Id
  @Column(name = "person_id")
  val personId: String,

  @JsonDateTimeConverter
  @Json(name = "updated_at")
  @Column(name = "updated_at", nullable = false)
  val updatedAt: LocalDateTime,

  @Json(name = "prison_number")
  @Column(name = "prison_number")
  val prisonNumber: String?,

  @Json(name = "latest_nomis_booking_id")
  @Column(name = "latest_nomis_booking_id")
  val latestNomisBookingId: Int? = null,

  @Json(name = "first_names")
  @Column(name = "first_names")
  val firstNames: String? = null,

  @Json(name = "last_name")
  @Column(name = "last_name")
  val lastName: String? = null,

  @JsonDateConverter
  @Json(name = "date_of_birth")
  @Column(name = "date_of_birth")
  val dateOfBirth: LocalDate? = null,

  @Json(name = "gender")
  @Column(name = "gender")
  val gender: String? = null,

  @Json(name = "ethnicity")
  @Column(name = "ethnicity")
  val ethnicity: String? = null,

) {
  companion object {
    fun fromJson(json: String): Person? = Klaxon().fieldConverter(JsonDateConverter::class, jsonDateConverter)
      .fieldConverter(JsonDateTimeConverter::class, jsonDateTimeConverter).parse<Person>(json)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Person

    if (personId != other.personId) return false
    if (updatedAt != other.updatedAt) return false
    if (prisonNumber != other.prisonNumber) return false
    if (latestNomisBookingId != other.latestNomisBookingId) return false
    if (firstNames != other.firstNames) return false
    if (lastName != other.lastName) return false
    if (dateOfBirth != other.dateOfBirth) return false
    if (gender != other.gender) return false
    if (ethnicity != other.ethnicity) return false

    return true
  }

  override fun hashCode(): Int {
    var result = personId.hashCode()
    result = 31 * result + updatedAt.hashCode()
    result = 31 * result + (prisonNumber?.hashCode() ?: 0)
    result = 31 * result + (latestNomisBookingId ?: 0)
    result = 31 * result + (firstNames?.hashCode() ?: 0)
    result = 31 * result + (lastName?.hashCode() ?: 0)
    result = 31 * result + (dateOfBirth?.hashCode() ?: 0)
    result = 31 * result + (gender?.hashCode() ?: 0)
    result = 31 * result + (ethnicity?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String = "Person(personId='$personId', updatedAt=$updatedAt, prisonNumber=XXXXXX, latestNomisBookingId=XXXXXX, firstNames=XXXXXX, lastName=XXXXXX, dateOfBirth=XXXXXX, gender=XXXXXX, ethnicity=XXXXXX)"
}
