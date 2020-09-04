package uk.gov.justice.digital.hmpps.pecs.jpc.location

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class Location(
        @NotNull @Size(min = 1, max = 2) val locationType: String,
        @Column(unique = true, nullable = false) @Size(min = 1) val nomisAgencyId: String,
        @Column(unique = true, nullable = false) @Size(min = 1) val siteName: String,
        @NotNull val addedAt: LocalDateTime = LocalDateTime.now(),
        @Id val id: UUID = UUID.randomUUID())
