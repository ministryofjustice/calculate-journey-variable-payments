package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate
import java.time.Month

/**
 * This repository exists due to the query performance and standard Spring JPA one's being too slow.
 */
@Component
class MoveQueryRepository(@Autowired val jdbcTemplate: JdbcTemplate) {

  fun moveCountInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate): Int {
    val rowMapper: RowMapper<Int> = RowMapper<Int> { resultSet: ResultSet, _: Int ->
      resultSet.getInt("moves_count")
    }
    return jdbcTemplate.query(
      "select count(move_id) as moves_count from moves m " +
        "where m.move_type is not null and m.supplier = ? and m.move_month = ? and m.move_year = ? and drop_off_or_cancelled is not null",
      rowMapper,
      supplier.name,
      startDate.month.value,
      startDate.year,
    )[0]
  }

  fun summariesInDateRange(
    supplier: Supplier,
    startDate: LocalDate,
    endDateInclusive: LocalDate,
    totalMoves: Int,
  ): List<MovesSummary> {
    val movesSummaryRowMapper = RowMapper { resultSet: ResultSet, _: Int ->
      with(resultSet) {
        val count = getInt("moves_count")
        MovesSummary(
          moveType = MoveType.valueOfCaseInsensitive(getString("move_type")),
          percentage = count.toDouble() / totalMoves,
          volume = count,
          volumeUnpriced = getInt("count_unpriced"),
          totalPriceInPence = getInt("total_price_in_pence"),
        )
      }
    }

    val movesSummarySQL = "select m.move_type, " +
      " count(s.move_id) as moves_count, " +
      " sum(s.move_price_in_pence) as total_price_in_pence, " +
      " sum(s.move_unpriced) as count_unpriced " +
      " from MOVES m inner join (" +
      " select sm.move_id, " +
      " sum(case when j.billable then COALESCE(pe.price_in_pence, p.price_in_pence) else 0 end) as move_price_in_pence, " +
      " max(case when p.price_in_pence is null then 1 else 0 end) as move_unpriced " +
      " from MOVES sm " +
      " left join JOURNEYS j on j.move_id = sm.move_id " +
      " left join LOCATIONS jfl on j.from_nomis_agency_id = jfl.nomis_agency_id " +
      " left join LOCATIONS jtl on j.to_nomis_agency_id = jtl.nomis_agency_id " +
      " left join PRICES p on jfl.location_id = p.from_location_id and jtl.location_id = p.to_location_id and j.effective_year = p.effective_year and p.supplier = ?" +
      " left join PRICE_EXCEPTIONS pe on p.price_id = pe.price_id and pe.month = ?" +
      " where sm.move_type is not null and sm.supplier = ? and sm.move_month = ? and sm.move_year = ? and sm.drop_off_or_cancelled is not null" +
      " group by sm.move_id) as s on m.move_id = s.move_id " +
      "GROUP BY m.move_type"

    return jdbcTemplate.query(
      movesSummarySQL,
      movesSummaryRowMapper,
      supplier.name,
      startDate.possiblePriceExceptionMonth(),
      supplier.name,
      startDate.month.value,
      startDate.year,
    )
  }

  private fun LocalDate.possiblePriceExceptionMonth() = this.month.value

  val moveJourneySelectSQL =
    """
        select  
        m.move_id, m.profile_id, m.updated_at, m.supplier, m.status, m.move_type,  
        m.reference, m.move_date, m.from_nomis_agency_id, m.to_nomis_agency_id, m.pick_up, m.drop_off_or_cancelled, m.notes,  
        m.report_from_location_type, m.report_to_location_type,
        m.cancellation_reason, m.cancellation_reason_comment,
        pp.person_id, pp.updated_at as person_updated_at, pp.prison_number,
        pp.first_names, pp.last_name, pp.date_of_birth, pp.latest_nomis_booking_id, pp.gender, pp.ethnicity,  
        fl.site_name as from_site_name, fl.location_type as from_location_type,  
        tl.site_name as to_site_name, tl.location_type as to_location_type,  
        j.journey_id, j.effective_year, j.supplier as journey_supplier, j.client_timestamp as journey_client_timestamp,  
        j.updated_at as journey_updated_at, j.billable,
        j.vehicle_registration as journey_vehicle_reg, j.state as journey_state,  
        j.from_nomis_agency_id as journey_from_nomis_agency_id, j.to_nomis_agency_id as journey_to_nomis_agency_id,  
        j.pick_up as journey_pick_up, j.drop_off as journey_drop_off, j.notes as journey_notes,  
        jfl.site_name as journey_from_site_name, jfl.location_type as journey_from_location_type,  
        jtl.site_name as journey_to_site_name, jtl.location_type as journey_to_location_type,  
        CASE WHEN j.billable THEN COALESCE(pe.price_in_pence, p.price_in_pence) ELSE NULL END as price_in_pence  
        from MOVES m 
            left join PROFILES pr on m.profile_id = pr.profile_id
            left join PEOPLE pp on pr.person_id = pp.person_id
            left join LOCATIONS fl on m.from_nomis_agency_id = fl.nomis_agency_id  
            left join LOCATIONS tl on m.to_nomis_agency_id = tl.nomis_agency_id  
            left join JOURNEYS j on j.move_id = m.move_id  
            left join LOCATIONS jfl on j.from_nomis_agency_id = jfl.nomis_agency_id  
            left join LOCATIONS jtl on j.to_nomis_agency_id = jtl.nomis_agency_id  
            left join PRICES p on jfl.location_id = p.from_location_id and jtl.location_id = p.to_location_id and j.effective_year = p.effective_year and p.supplier = ?
            left join PRICE_EXCEPTIONS pe on p.price_id = pe.price_id and pe.month = ?
    """.trimIndent()

  val moveJourneyRowMapper = RowMapper { resultSet: ResultSet, _: Int ->
    with(resultSet) {
      val move = Move(
        moveId = getString("move_id"),
        profileId = getString("profile_id"),
        updatedAt = getTimestamp("updated_at").toLocalDateTime(),
        supplier = Supplier.valueOfCaseInsensitive(getString("supplier")),
        status = MoveStatus.valueOfCaseInsensitive(getString("status")),
        moveType = getString("move_type")?.let { MoveType.valueOfCaseInsensitive(it) },
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
        reportFromLocationType = getString("report_from_location_type"),
        reportToLocationType = getString("report_to_location_type"),
        cancellationReason = getString("cancellation_reason"),
        cancellationReasonComment = getString("cancellation_reason_comment"),
      )

      val personId = getString("person_id")
      val person = personId?.let {
        // There is a person for this move
        Person(
          personId = it,
          updatedAt = getTimestamp("person_updated_at").toLocalDateTime(),
          prisonNumber = getString("prison_number"),
          latestNomisBookingId = getInt("latest_nomis_booking_id"),
          firstNames = getString("first_names"),
          lastName = getString("last_name"),
          ethnicity = getString("ethnicity"),
          gender = getString("gender"),
          dateOfBirth = getDate("date_of_birth")?.toLocalDate(),
        )
      }

      val journeyId = getString("journey_id")
      val journey = journeyId?.let {
        // there is a journey for this move
        Journey(
          journeyId = it,
          updatedAt = getTimestamp("journey_updated_at").toLocalDateTime(),
          moveId = getString("move_id"),
          state = JourneyState.valueOfCaseInsensitive(getString("journey_state")),
          supplier = Supplier.valueOfCaseInsensitive(getString("journey_supplier")),
          clientTimeStamp = getTimestamp("journey_client_timestamp")?.toLocalDateTime(),
          fromNomisAgencyId = getString("journey_from_nomis_agency_id"),
          fromSiteName = getString("journey_from_site_name"),
          fromLocationType = getString("journey_from_location_type")?.let { LocationType.valueOf(it) },
          toNomisAgencyId = getString("journey_to_nomis_agency_id"),
          toSiteName = getString("journey_to_site_name"),
          toLocationType = getString("journey_to_location_type")?.let { LocationType.valueOf(it) },
          pickUpDateTime = getTimestamp("journey_pick_up")?.toLocalDateTime(),
          dropOffDateTime = getTimestamp("journey_drop_off")?.toLocalDateTime(),
          vehicleRegistration = getString("journey_vehicle_reg"),
          billable = getBoolean("billable"),
          notes = getString("journey_notes"),
          priceInPence = if (getInt("price_in_pence") == 0 && wasNull()) null else getInt("price_in_pence"),
          effectiveYear = getInt("effective_year"),
        )
      }

      MovePersonJourney(move, person, journey)
    }
  }

  /**
   * Excludes moves without a move_type. This is an indication of bad reporting data in the JSON data feeds.
   */
  fun moveWithPersonAndJourneys(moveId: String, supplier: Supplier, inMonth: Month): Move? {
    val movePersonJourney = jdbcTemplate.query(
      "$moveJourneySelectSQL where m.move_id = ? and m.supplier = ? and m.move_type is not null",
      moveJourneyRowMapper,
      supplier.name,
      inMonth.value,
      moveId,
      supplier.name,
    ).groupBy { it.move.moveId }

    return movePersonJourney.keys.map { k -> movePersonJourney.getValue(k).move() }.firstOrNull()
  }

  fun movesForMoveTypeInDateRange(
    supplier: Supplier,
    moveType: MoveType,
    startDate: LocalDate,
    endDateInclusive: LocalDate,
    limit: Int = 50,
    offset: Long = 0,
  ): List<Move> {
    val movesWithPersonAndJourneys = jdbcTemplate.query(
      "$moveJourneySelectSQL where m.supplier = ? and m.move_month = ? and m.move_year = ? and m.move_type = ? and m.drop_off_or_cancelled is not null " +
        "order by m.drop_off_or_cancelled, journey_drop_off NULLS LAST ",
      moveJourneyRowMapper,
      supplier.name,
      startDate.possiblePriceExceptionMonth(),
      supplier.name,
      startDate.month.value,
      startDate.year,
      moveType.name,
    ).groupBy { it.move.moveId }

    return movesWithPersonAndJourneys.keys.map { k ->
      movesWithPersonAndJourneys.getValue(k).move()
    }
  }

  fun movesWithMoveTypeInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate) = MoveType.values().associateBy({ it }, { movesForMoveTypeInDateRange(supplier, it, startDate, endDateInclusive) })

  /**
   * This will include moves with and without a move type assigned in the results.
   */
  fun allMovesInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate): List<Move> {
    val movesWithPersonAndJourneys = jdbcTemplate.query(
      "$moveJourneySelectSQL where m.supplier = ? and m.drop_off_or_cancelled >= ? and m.drop_off_or_cancelled < ? " +
        "order by m.drop_off_or_cancelled, journey_drop_off NULLS LAST ",
      moveJourneyRowMapper,
      supplier.name,
      startDate.possiblePriceExceptionMonth(),
      supplier.name,
      Timestamp.valueOf(startDate.atStartOfDay()),
      Timestamp.valueOf(endDateInclusive.plusDays(1).atStartOfDay()),
    ).groupBy { it.move.moveId }

    return movesWithPersonAndJourneys.keys.map { k ->
      movesWithPersonAndJourneys.getValue(k).move()
    }
  }

  class MovePersonJourney(val move: Move, val person: Person?, val journey: Journey?)

  fun List<MovePersonJourney>.move() = this[0].move.copy(
    journeys = this.mapNotNull { it.journey },
    person = this[0].person,
  )
}
