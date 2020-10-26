package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "JOURNEYS")
data class JourneyModel(
        @Id
        @Column(name = "journey_id")
        val journeyId: String,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "move_id")
        val move: MoveModel,

        @Enumerated(EnumType.STRING)
        val state: JourneyState,

        @Column(name = "from_nomis_agency_id", nullable = false)
        val fromNomisAgencyId: String,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "from_location_id")
        val fromLocation: Location? = null,

        @Column(name = "to_nomis_agency_id", nullable = false)
        val toNomisAgencyId: String,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "to_location_id")
        val toLocation: Location? = null,

        @Column(name = "pick_up", nullable = true)
        val pickUp: LocalDateTime? = null,

        @Column(name = "drop_off", nullable = true)
        val dropOff: LocalDateTime? = null,

        @Column(name = "vehicle_registration", nullable = true)
        val vehicleRegistation: String? = null,

        val billable: Boolean,

        val notes: String? = null,

        @ManyToOne
        @JoinColumns(
                JoinColumn(name = "from_location_id", referencedColumnName = "from_location_id", insertable = false, updatable = false),
                JoinColumn(name = "to_location_id", referencedColumnName = "to_location_id", insertable = false, updatable = false)
        )
        val price: Price? = null

        ) {
        override fun toString(): String {
                return "JourneyModel(journeyId=$journeyId, fromNomisAgencyId='$fromNomisAgencyId', fromLocation=$fromLocation, toNomisAgencyId='$toNomisAgencyId', toLocation=$toLocation, billable=$billable, notes=$notes)"
        }
}