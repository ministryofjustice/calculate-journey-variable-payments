package uk.gov.justice.digital.hmpps.pecs.jpc.location

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Enumerated
import javax.persistence.EnumType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "LOCATIONS")
data class Location(
        @Enumerated(EnumType.STRING)
        val locationType: LocationType,

        @Column(unique = true, nullable = false)
        @get: NotBlank(message = "NOMIS agency id cannot be blank")
        val nomisAgencyId: String,

        @Column(unique = true, nullable = false)
        @get: NotBlank(message = "site name cannot be blank")
        val siteName: String,

        @Column(nullable = false)
        val addedAt: LocalDateTime = LocalDateTime.now(),

        @Id
        @Column(name = "location_id", nullable = false)
        val id: UUID = UUID.randomUUID()
)
