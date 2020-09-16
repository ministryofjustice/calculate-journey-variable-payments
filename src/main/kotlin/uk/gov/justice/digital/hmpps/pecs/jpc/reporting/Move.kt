package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.time.LocalDate
import java.util.UUID
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "MOVES")
data class Move(

        @EventUUID
        @Id
        @Column(name = "move_id", nullable = false)
        val id: UUID,

        @Column(nullable = false)
        @get: NotBlank(message = "reference cannot be blank")
        val reference: String,

        @EventDate
        val date: LocalDate? = null,

        @Column(nullable = false)
        @get: NotBlank(message = "status cannot be blank")
        val status: String,

        @Json(name = "from_location")
        @Column(nullable = false)
        @get: NotBlank(message = "from location cannot be blank")
        val fromLocation: String,

        @Json(name = "to_location")
        val toLocation: String? = null,

        @Json(ignored = true)
        @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true )
        @JoinColumn(name = "eventableId")
        @org.hibernate.annotations.ForeignKey( name = "none")
        val events: Set<Event> = setOf<Event>(),

        @Json(ignored = true)
        @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
        @JoinColumn(name = "moveId")
        val journeys: Set<Journey> = setOf<Journey>()

) {
    companion object {
        fun fromJson(json: String): Move? {
            return Klaxon().
            fieldConverter(EventDate::class, dateConverter).
            fieldConverter(EventUUID::class, uuidConverter).
            parse<Move>(json)
        }
    }
}