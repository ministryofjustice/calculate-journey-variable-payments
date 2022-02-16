package uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.EventDateTime
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.dateTimeConverter
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "PROFILES")
data class Profile(
  @Json(name = "id")
  @Id
  @Column(name = "profile_id")
  val profileId: String,

  @EventDateTime
  @Json(name = "updated_at")
  @Column(name = "updated_at", nullable = false)
  val updatedAt: LocalDateTime,

  @Json(name = "person_id")
  @Column(name = "person_id", nullable = false)
  val personId: String,
) {
  companion object {
    fun fromJson(json: String): Profile? {
      return Klaxon().fieldConverter(EventDateTime::class, dateTimeConverter).parse<Profile>(json)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Profile

    if (profileId != other.profileId) return false
    if (updatedAt != other.updatedAt) return false
    if (personId != other.personId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = profileId.hashCode()
    result = 31 * result + updatedAt.hashCode()
    result = 31 * result + personId.hashCode()
    return result
  }

  override fun toString(): String {
    return "Profile(profileId='$profileId', updatedAt=$updatedAt, personId='$personId')"
  }
}
