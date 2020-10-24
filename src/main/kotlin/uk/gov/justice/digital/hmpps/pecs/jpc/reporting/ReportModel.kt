package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "REPORTS")
// TODO add moveId, moveRef, move status, moveStartDateTime, moveCompleteDateTime, moveCancelDateTime, from_agency_id, from_location (outer join), to_agency_id, to_location (outer join), prison_id, notes
data class ReportModel(
        @Id
        @Column(name = "id", nullable = false)
        val id: UUID = UUID.randomUUID(),

        @Enumerated(EnumType.STRING)
        val supplier: Supplier,

        @Enumerated(EnumType.STRING)
        val movePriceType: MovePriceType,

        @Column(name = "move_date", nullable = true)
        val moveDate: LocalDate? = null,

        @Column(name = "report", nullable = false)
        val report: String,

        @OneToMany(
                mappedBy = "report",
                cascade = arrayOf(CascadeType.ALL),
                orphanRemoval = true
        )
        val journeys: MutableList<ReportJourneyModel> = mutableListOf()

        ) {

        fun addJourneys(vararg journeyModels: ReportJourneyModel) {
                journeys += journeyModels
        }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as ReportModel

                if (id != other.id) return false
                if (supplier != other.supplier) return false
                if (movePriceType != other.movePriceType) return false
                if (moveDate != other.moveDate) return false
                if (report != other.report) return false

                return true
        }

        override fun hashCode(): Int {
                var result = id.hashCode()
                result = 31 * result + supplier.hashCode()
                result = 31 * result + movePriceType.hashCode()
                result = 31 * result + (moveDate?.hashCode() ?: 0)
                result = 31 * result + report.hashCode()
                return result
        }

        override fun toString(): String {
                return "ReportModel(id=$id, supplier=$supplier, movePriceType=$movePriceType, moveDate=$moveDate, report='$report')"
        }
}