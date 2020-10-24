package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "JOURNEYS")
data class ReportJourneyModel(
        @Id
        @Column(name = "id", nullable = false)
        val id: UUID = UUID.randomUUID(),

        @ManyToOne(fetch = FetchType.LAZY)
        val report: ReportModel,

        val fromNomisAgencyId: String,

        val toNomisAgencyId: String,

        val billable: Boolean
        ) {
}