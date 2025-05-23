package uk.gov.justice.digital.hmpps.pecs.jpc.domain.location

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "LOCATIONS")
data class Location(
  @Enumerated(EnumType.STRING)
  @Column(name = "location_type", nullable = false)
  var locationType: LocationType,

  @Column(name = "nomis_agency_id", unique = true, nullable = false)
  @get:NotBlank(message = "NOMIS agency id cannot be blank")
  val nomisAgencyId: String,

  @Column(name = "site_name", unique = true, nullable = false)
  @get:NotBlank(message = "site name cannot be blank")
  var siteName: String,

  @Column(name = "added_at", nullable = false)
  val addedAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "updated_at", nullable = false)
  var updatedAt: LocalDateTime? = LocalDateTime.now(),

  @Id
  @Column(name = "location_id", nullable = false)
  val id: UUID = UUID.randomUUID(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Location

    if (locationType != other.locationType) return false
    if (nomisAgencyId != other.nomisAgencyId) return false
    if (siteName != other.siteName) return false
    if (addedAt != other.addedAt) return false
    if (updatedAt != other.updatedAt) return false
    if (id != other.id) return false

    return true
  }

  override fun hashCode(): Int {
    var result = locationType.hashCode()
    result = 31 * result + nomisAgencyId.hashCode()
    result = 31 * result + siteName.hashCode()
    result = 31 * result + addedAt.hashCode()
    result = 31 * result + (updatedAt?.hashCode() ?: 0)
    result = 31 * result + id.hashCode()
    return result
  }

  override fun toString(): String = "Location(locationType=$locationType, nomisAgencyId='$nomisAgencyId', siteName='$siteName', addedAt=$addedAt, updatedAt=$updatedAt, id=$id)"
}
