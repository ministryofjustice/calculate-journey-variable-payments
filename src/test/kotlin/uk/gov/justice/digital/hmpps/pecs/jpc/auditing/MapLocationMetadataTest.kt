package uk.gov.justice.digital.hmpps.pecs.jpc.auditing

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType

internal class MapLocationMetadataTest {
  @Test
  fun `mapping a new location`() {
    val metadata = MapLocationMetadata.map(Location(LocationType.PR, "AGENCY_ID", "SITE NAME"))

    assertThat(metadata.nomisId).isEqualTo("AGENCY_ID")
    assertThat(metadata.newName).isEqualTo("SITE NAME")
    assertThat(metadata.newType).isEqualTo(LocationType.PR)
    assertThat(metadata.oldName).isNull()
    assertThat(metadata.oldType).isNull()
    assertThat(metadata.isRemapping()).isFalse
  }

  @Test
  fun `remapping of existing location name`() {
    val existingLocation = Location(LocationType.PR, "AGENCY_ID", "OLD SITE NAME")
    val metadata = MapLocationMetadata.remap(existingLocation, existingLocation.copy(siteName = "NEW SITE NAME"))

    assertThat(metadata.nomisId).isEqualTo("AGENCY_ID")
    assertThat(metadata.newName).isEqualTo("NEW SITE NAME")
    assertThat(metadata.newType).isNull()
    assertThat(metadata.oldName).isEqualTo("OLD SITE NAME")
    assertThat(metadata.oldType).isNull()
  }

  @Test
  fun `remapping of existing location type`() {
    val existingLocation = Location(LocationType.PR, "AGENCY_ID", "SITE NAME")
    val metadata = MapLocationMetadata.remap(existingLocation, existingLocation.copy(locationType = LocationType.AP))

    assertThat(metadata.nomisId).isEqualTo("AGENCY_ID")
    assertThat(metadata.newName).isNull()
    assertThat(metadata.newType).isEqualTo(LocationType.AP)
    assertThat(metadata.oldName).isNull()
    assertThat(metadata.oldType).isEqualTo(LocationType.PR)
    assertThat(metadata.isRemapping()).isTrue
  }

  @Test
  fun `remapping of existing location name and type`() {
    val existingLocation = Location(LocationType.PR, "AGENCY_ID", "OLD SITE NAME")
    val metadata = MapLocationMetadata.remap(existingLocation, existingLocation.copy(siteName = "NEW SITE NAME", locationType = LocationType.AP))

    assertThat(metadata.nomisId).isEqualTo("AGENCY_ID")
    assertThat(metadata.newName).isEqualTo("NEW SITE NAME")
    assertThat(metadata.newType).isEqualTo(LocationType.AP)
    assertThat(metadata.oldName).isEqualTo("OLD SITE NAME")
    assertThat(metadata.oldType).isEqualTo(LocationType.PR)
    assertThat(metadata.isRemapping()).isTrue
  }

  @Test
  fun `remapping fails when not same locations`() {
    val locationA = Location(LocationType.PR, "AGENCY_A", "OLD SITE NAME")
    val locationB = Location(LocationType.PR, "AGENCY_B", "OLD SITE NAME")

    assertThatThrownBy { MapLocationMetadata.remap(locationA, locationB) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Different locations provided.")
  }

  @Test
  fun `remapping fails when no actual location changes`() {
    val location = Location(LocationType.PR, "AGENCY_A", "OLD SITE NAME")

    assertThatThrownBy { MapLocationMetadata.remap(location, location.copy()) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Old and new location data is exactly the same.")
  }
}
