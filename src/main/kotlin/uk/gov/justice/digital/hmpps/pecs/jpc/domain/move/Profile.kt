package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

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
}