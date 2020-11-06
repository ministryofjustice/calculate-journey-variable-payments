package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate

@Component
class MoveQueryRepository(@Autowired val jdbcTemplate: JdbcTemplate) {

    fun countForSupplierInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate): Int {
        val rowMapper: RowMapper<Int> = RowMapper<Int> { resultSet: ResultSet, _: Int ->
            resultSet.getInt("moves_count")
        }
        return jdbcTemplate.query("select count(move_id) as moves_count from moves " +
                "where supplier = ? and drop_off_or_cancelled >= ? and drop_off_or_cancelled < ?",
                arrayOf(
                        supplier.name,
                        Timestamp.valueOf(startDate.atStartOfDay()),
                        Timestamp.valueOf(endDateInclusive.plusDays(1).atStartOfDay())
                ),
                rowMapper)[0]
    }

    fun summariesForSupplierInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate, totalMoves: Int): List<MovesSummary> {
        val rowMapper = RowMapper<MovesSummary> { resultSet: ResultSet, _: Int ->
            with(resultSet) {
                val count = getInt("moves_count")
                MovesSummary(
                    moveType = MoveType.valueOfCaseInsensitive(getString("move_price_type")),
                    percentage = count.toDouble() / totalMoves,
                    volume = count,
                    volumeUnpriced = getInt("count_unpriced"),
                    totalPriceInPence = getInt("total_price_in_pence")
                )
            }
        }

        val sql = "select m.move_price_type, " +
                " count(s.move_id) as moves_count, " +
                " sum(s.move_price_in_pence) as total_price_in_pence, " +
                " sum(s.move_unpriced) as count_unpriced " +
                " from MOVES m inner join (" +
                " select sm.move_id, " +
                " sum(case when j.billable then p.price_in_pence else 0 end) as move_price_in_pence, " +
                " max(case when p.price_in_pence is null then 1 else 0 end) as move_unpriced " +
                " from MOVES sm " +
                " left join JOURNEYS j on j.move_id = sm.move_id " +
                " left join LOCATIONS jfl on j.from_nomis_agency_id = jfl.nomis_agency_id " +
                " left join LOCATIONS jtl on j.to_nomis_agency_id = jtl.nomis_agency_id " +
                " left join PRICES p on jfl.location_id = p.from_location_id and jtl.location_id = p.to_location_id " +
                " where sm.supplier = ? and sm.drop_off_or_cancelled >= ? and sm.drop_off_or_cancelled < ? " +
                " group by sm.move_id) as s on m.move_id = s.move_id " +
                "GROUP BY m.move_price_type"

        return jdbcTemplate.query(sql,
                arrayOf(
                        supplier.name,
                        Timestamp.valueOf(startDate.atStartOfDay()),
                        Timestamp.valueOf(endDateInclusive.plusDays(1).atStartOfDay())
                ),
                rowMapper)
    }

    fun allForSupplierAndMoveTypeInDateRange(supplier: Supplier, moveType: MoveType, startDate: LocalDate, endDateInclusive: LocalDate, limit: Int = 50, offset: Long = 0): List<Move> {
        val rowMapper = RowMapper { resultSet: ResultSet, _: Int ->
            with(resultSet) {
                val move = Move(
                        moveId = getString("move_id"),
                        supplier = Supplier.valueOfCaseInsensitive(getString("supplier")),
                        status = MoveStatus.valueOfCaseInsensitive(getString("status")),
                        moveType = MoveType.valueOfCaseInsensitive(getString("move_price_type")),
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
                val journey = journeyId?.let { // there is a journey for this move
                    Journey(
                            journeyId = journeyId,
                            moveId = getString("move_id"),
                            state = JourneyState.valueOfCaseInsensitive(getString("journey_state")),
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

                MoveAndJourney(move, journey)
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
                "where m.supplier = ? and m.move_price_type = ? and m.drop_off_or_cancelled >= ? and m.drop_off_or_cancelled < ? " +
                "order by m.drop_off_or_cancelled, journey_drop_off NULLS LAST " +
                "LIMIT $limit OFFSET $offset",
                arrayOf(
                        supplier.name,
                        moveType.name,
                        Timestamp.valueOf(startDate.atStartOfDay()),
                        Timestamp.valueOf(endDateInclusive.plusDays(1).atStartOfDay())
                ),
                rowMapper).groupBy { it.move.moveId }

        return movesAndJourneys.keys.map { k ->
            val mjs = movesAndJourneys.getValue(k)
            mjs[0].move.copy(journeys = mjs.mapNotNull { it.journey }.toMutableList())
        }
    }

    fun uniqueJourneysSummaryForSupplierInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate): JourneysSummary{
        val rowMapper = RowMapper<JourneysSummary> { resultSet: ResultSet, _: Int ->
            with(resultSet) {
                JourneysSummary(
                        count = getInt("unique_journeys"),
                        totalPriceInPence = getInt("total_price_in_pence"),
                        countWithoutLocations = getInt("count_without_locations"),
                        countUnpriced = getInt("count_unpriced")
                        )
            }
        }

        val sql = "select count(js.journey) as unique_journeys,\n" +
                "       sum(js.price_in_pence) as total_price_in_pence,\n" +
                "       sum (js.volume_unlocationed) as count_without_locations,\n" +
                "       sum(js.volume_unpriced) as count_unpriced from(\n" +
                "            select distinct CONCAT(j.from_nomis_agency_id, ' ', j.to_nomis_agency_id) as journey,\n" +
                "                            max(case when jfl.location_id is null or jtl.location_id is null then 1 else 0 end) as volume_unlocationed,\n" +
                "                            sum(case when j.billable then p.price_in_pence else 0 end) as price_in_pence,\n" +
                "                            max(case when p.price_in_pence is null then 1 else 0 end) as volume_unpriced\n" +
                "             from MOVES m\n" +
                "             inner join JOURNEYS j on j.move_id = m.move_id " +
                "             left join LOCATIONS jfl on j.from_nomis_agency_id = jfl.nomis_agency_id\n" +
                "             left join LOCATIONS jtl on j.to_nomis_agency_id = jtl.nomis_agency_id\n" +
                "             left join PRICES p on jfl.location_id = p.from_location_id and jtl.location_id = p.to_location_id\n" +
                "             where m.supplier = ? and m.drop_off_or_cancelled >= ? and m.drop_off_or_cancelled < ? " +
                "\n" +
                "    GROUP BY journey) as js"

        return jdbcTemplate.query(sql,
                arrayOf(
                        supplier.name,
                        Timestamp.valueOf(startDate.atStartOfDay()),
                        Timestamp.valueOf(endDateInclusive.plusDays(1).atStartOfDay())
                ),
                rowMapper)[0]
    }

    fun allForSupplierInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate) =
            MoveType.values().map { allForSupplierAndMoveTypeInDateRange(supplier, it, startDate, endDateInclusive) }

    class MoveAndJourney(val move: Move, val journey: Journey?)
}

