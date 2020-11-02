package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePriceType
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate

@Component
class MoveModelJdbcRepository(@Autowired val jdbcTemplate: JdbcTemplate) {

    fun findAllForSupplierAndMovePriceTypeInDateRange(supplier: Supplier, movePriceType: MovePriceType, startDate: LocalDate, endDateInclusive: LocalDate): List<MoveModel> {
        val rowMapper: RowMapper<MoveAndJourneyModel> = RowMapper<MoveAndJourneyModel> { resultSet: ResultSet, _: Int ->
            with(resultSet) {
                val moveModel = MoveModel(
                        moveId = getString("move_id"),
                        supplier = Supplier.valueOf(getString("supplier")),
                        status = MoveStatus.valueOf(getString("status")),
                        movePriceType = MovePriceType.valueOf(getString("move_price_type")),
                        reference = getString("reference"),
                        moveDate = getDate("move_date")?.toLocalDate(),
                        fromNomisAgencyId = getString("from_nomis_agency_id"),
                        fromSiteName = getString("from_site_name"),
                        fromLocationType = getString("from_location_type")?.let { LocationType.valueOf(it) },
                        toNomisAgencyId = getString("to_nomis_agency_id"),
                        toSiteName = getString("to_site_name"),
                        toLocationType = getString("to_location_type")?.let { LocationType.valueOf(it) },
                        pickUpDateTime = getTimestamp("pick_up")?.toLocalDateTime(),
                        dropOffOrCancelledDateTime = getTimestamp("drop_off_or_cancelled")?.toLocalDateTime(),
                        notes = getString("notes"),
                        prisonNumber = getString("prison_number"),
                        vehicleRegistration = getString("vehicle_registration")
                )
                val journeyId = getString("journey_id")
                val journeyModel = journeyId?.let{ // there is a journey for this move
                JourneyModel(
                        journeyId = journeyId,
                        moveId = getString("move_id"),
                        state = JourneyState.valueOf(getString("journey_state")),
                        fromNomisAgencyId = getString("journey_from_nomis_agency_id"),
                        fromSiteName = getString("journey_from_site_name"),
                        fromLocationType = getString("journey_from_location_type")?.let { LocationType.valueOf(it) },
                        toNomisAgencyId = getString("journey_to_nomis_agency_id"),
                        toSiteName = getString("journey_to_site_name"),
                        toLocationType = getString("journey_to_location_type")?.let { LocationType.valueOf(it) },
                        pickUpDateTime = getTimestamp("journey_pick_up")?.toLocalDateTime(),
                        dropOffDateTime = getTimestamp("journey_drop_off")?.toLocalDateTime(),
                        vehicleRegistration = getString("vehicle_registration"),
                        billable = getBoolean("billable"),
                        notes = getString("journey_notes"),
                        priceInPence = if (getInt("price_in_pence") == 0 && wasNull()) null else getInt("price_in_pence")
                )
            }

                MoveAndJourneyModel(moveModel, journeyModel)
            }
        }
        val movesAndJourneys = jdbcTemplate.query("select " +
                "m.move_id, m.supplier, m.status, m.move_price_type, " +
                "m.reference, m.move_date, m.from_nomis_agency_id, m.to_nomis_agency_id, m.pick_up, m.drop_off_or_cancelled, m.notes, " +
                "m.prison_number, m.vehicle_registration, " +
                "fl.site_name as from_site_name, fl.location_type as from_location_type, " +
                "tl.site_name as to_site_name, tl.location_type as to_location_type, " +
                "j.journey_id, j.billable, j.vehicle_registration, j.state as journey_state, " +
                "j.from_nomis_agency_id as journey_from_nomis_agency_id, j.to_nomis_agency_id as journey_to_nomis_agency_id, " +
                "j.pick_up as journey_pick_up, j.drop_off as journey_drop_off, j.notes as journey_notes, " +
                "jfl.site_name as journey_from_site_name, jfl.location_type as journey_from_location_type, " +
                "jtl.site_name as journey_to_site_name, jtl.location_type as journey_to_location_type, " +
                "CASE WHEN j.billable THEN p.price_in_pence ELSE NULL END as price_in_pence " +
                "from MOVES m " +
                "left join LOCATIONS fl on m.from_nomis_agency_id = fl.nomis_agency_id " +
                "left join LOCATIONS tl on m.to_nomis_agency_id = tl.nomis_agency_id " +
                "left join JOURNEYS j on j.move_id = m.move_id " +
                "left join LOCATIONS jfl on j.from_nomis_agency_id = jfl.nomis_agency_id " +
                "left join LOCATIONS jtl on j.to_nomis_agency_id = jtl.nomis_agency_id " +
                "left join PRICES p on jfl.location_id = p.from_location_id and jtl.location_id = p.to_location_id " +
                "where m.supplier = ? and move_price_type = ? and m.drop_off_or_cancelled >= ? and m.drop_off_or_cancelled < ? " +
                "order by m.drop_off_or_cancelled, journey_drop_off NULLS LAST",
                arrayOf(
                        supplier.name,
                        movePriceType.name,
                        Timestamp.valueOf(startDate.atStartOfDay()),
                        Timestamp.valueOf(endDateInclusive.plusDays(1).atStartOfDay())
                ),
                rowMapper).groupBy { it.moveModel.moveId }

        return movesAndJourneys.keys.map { k ->
            val mjs = movesAndJourneys.getValue(k)
            mjs[0].moveModel.copy(journeys = mjs.mapNotNull { it.journeyModel }.toMutableList())
        }
    }

    fun findSummaryForSupplierInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate): MovePriceTypeWithMovesAndSummary {
        val allMoves = MovePriceType.values().map {
            findAllForSupplierAndMovePriceTypeInDateRange(supplier, it, startDate, endDateInclusive)
        }

        val totalSize = allMoves.flatten().size

        fun getMovesAndSummary(moves: List<MoveModel>) : MovesAndSummary {
            val percentage = if(moves.isEmpty()) 0.0 else  moves.size.toDouble() / totalSize
            val volume = moves.size
            val volumeUnpriced = moves.count { it.totalInPence() == null }
            val totalPrice = moves.sumBy { it.totalInPence() ?: 0 } // nulls priced as 0

            return MovesAndSummary(
                    moves,
                    Summary(percentage, volume, volumeUnpriced, totalPrice)
            )
        }

        return MovePriceTypeWithMovesAndSummary(
                standard = getMovesAndSummary(allMoves[0]),
                longHaul = getMovesAndSummary(allMoves[1]),
                redirection = getMovesAndSummary(allMoves[2]),
                lockout = getMovesAndSummary(allMoves[3]),
                multi = getMovesAndSummary(allMoves[4]),
                cancelled = getMovesAndSummary(allMoves[5])
        )
    }
    class MoveAndJourneyModel(val moveModel: MoveModel, val journeyModel: JourneyModel?)
}

data class MovePriceTypeWithMovesAndSummary(
        val standard: MovesAndSummary,
        val longHaul: MovesAndSummary,
        val redirection: MovesAndSummary,
        val lockout: MovesAndSummary,
        val multi: MovesAndSummary,
        val cancelled: MovesAndSummary
){
    fun summary() : Summary {
        return with(listOf(standard, longHaul, redirection, lockout, multi, cancelled)) {
            Summary(
                sumByDouble { it.summary.percentage },
                sumBy { it.summary.volume },
                sumBy { it.summary.volumeUnpriced },
                sumBy { it.summary.totalPriceInPence }
            )
        }
    }
}

data class MovesAndSummary(
        val moves: List<MoveModel>,
        val summary: Summary
)

data class Summary(
        val percentage: Double = 0.0,
        val volume: Int = 0,
        val volumeUnpriced: Int = 0,
        val totalPriceInPence: Int = 0) {
    val totalPriceInPounds = totalPriceInPence.toDouble() / 100
}