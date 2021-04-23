package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

/**
 * Metadata to capture mapping of new locations and remapping of existing locations.
 */
data class MapLocationMetadata(
  @Json(name = "nomis_id", index = 1)
  val nomisId: String,

  @Json(name = "new_name", index = 2, serializeNull = false)
  val newName: String? = null,

  @Json(name = "new_type", index = 3, serializeNull = false)
  val newType: LocationType? = null,

  @Json(name = "old_name", index = 4, serializeNull = false)
  val oldName: String? = null,

  @Json(name = "old_type", index = 5, serializeNull = false)
  val oldType: LocationType? = null
) : Metadata {
  private constructor(location: Location) : this(location.nomisAgencyId, location.siteName, location.locationType)

  private constructor(old: Location, new: Location) : this(
    nomisId = old.nomisAgencyId,
    newName = if (old.siteName != new.siteName) new.siteName else null,
    oldName = if (old.siteName != new.siteName) old.siteName else null,
    newType = if (old.locationType != new.locationType) new.locationType else null,
    oldType = if (old.locationType != new.locationType) old.locationType else null
  )

  init {
    if (newName == null && newType == null && oldName == null && oldType == null) throw IllegalStateException("Only NOMIS ID provided.")
  }

  companion object {
    fun map(location: Location) = MapLocationMetadata(location)

    fun remap(old: Location, new: Location): MapLocationMetadata {
      if (old.id != new.id) throw IllegalArgumentException("Different locations provided.")

      if (old == new) throw IllegalArgumentException("Old and new location data is exactly the same.")

      return MapLocationMetadata(old, new)
    }
  }

  fun isRemapping() = oldName != null || oldType != null

  override fun toJsonString(): String = Klaxon().toJsonString(this)
}
