package uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.sql.ResultSet
import java.time.LocalDate

/**
 * This repository exists due to the query performance and standard Spring JPA one's being too slow.
 */
@Component
class JourneyQueryRepository(@Autowired val jdbcTemplate: JdbcTemplate) {

  fun prices(
    supplier: Supplier,
    fromSiteName: String?,
    toSiteName: String?,
    effectiveYear: Int,
  ): List<JourneyWithPrice> {
    val selectPricesSQL =
      """
        select jfl.nomis_agency_id as journey_from_nomis_agency_id,
        jfl.site_name       as journey_from_site_name,
        jfl.location_type   as journey_from_location_type,
        jtl.nomis_agency_id as journey_to_nomis_agency_id,
        jtl.site_name       as journey_to_site_name,
        jtl.location_type   as journey_to_location_type,
        p.price_in_pence    as unit_price_in_pence
        from PRICES p
         inner join LOCATIONS jfl on p.from_location_id = jfl.location_id
         inner join LOCATIONS jtl on p.to_location_id = jtl.location_id
            where p.supplier = ? 
      """.trimIndent() +
        (if (fromSiteName.isNullOrBlank()) "" else " and jfl.site_name = ? ") +
        (if (toSiteName.isNullOrBlank()) "" else " and jtl.site_name = ? ") +
        " and p.effective_year = ? "

    val pricesRowMapper = RowMapper { resultSet: ResultSet, _: Int ->
      with(resultSet) {
        JourneyWithPrice(
          fromNomisAgencyId = getString("journey_from_nomis_agency_id"),
          fromLocationType = getString("journey_from_location_type")?.let { LocationType.valueOf(it) },
          fromSiteName = getString("journey_from_site_name"),
          toNomisAgencyId = getString("journey_to_nomis_agency_id"),
          toLocationType = getString("journey_to_location_type")?.let { LocationType.valueOf(it) },
          toSiteName = getString("journey_to_site_name"),
          unitPriceInPence = if (getInt("unit_price_in_pence") == 0 && wasNull()) null else getInt("unit_price_in_pence"),
          volume = null,
          totalPriceInPence = null,
        )
      }
    }

    val stringPlaceholders = listOf(supplier.name, fromSiteName, toSiteName).filter { !it.isNullOrBlank() }
    val placeholders = (stringPlaceholders + effectiveYear.toString()).toTypedArray()
    return jdbcTemplate.query(selectPricesSQL, pricesRowMapper, *placeholders)
  }

  fun distinctJourneysAndPriceInDateRange(
    supplier: Supplier,
    startDate: LocalDate,
    endDateInclusive: LocalDate,
    excludePriced: Boolean = true,
  ): List<JourneyWithPrice> {
    val journeyWithPricesRowMapper = RowMapper { resultSet: ResultSet, _: Int ->
      with(resultSet) {
        JourneyWithPrice(
          fromNomisAgencyId = getString("journey_from_nomis_agency_id"),
          fromLocationType = getString("journey_from_location_type")?.let { LocationType.valueOf(it) },
          fromSiteName = getString("journey_from_site_name"),
          toNomisAgencyId = getString("journey_to_nomis_agency_id"),
          toLocationType = getString("journey_to_location_type")?.let { LocationType.valueOf(it) },
          toSiteName = getString("journey_to_site_name"),
          volume = getInt("volume"),
          unitPriceInPence = if (getInt("unit_price_in_pence") == 0 && wasNull()) null else getInt("unit_price_in_pence"),
          totalPriceInPence = getInt("total_price_in_pence"),
        )
      }
    }

    val havingOnlyUnpriced =
      if (excludePriced) "HAVING max(case when p.price_in_pence is null then 1 else 0 end) > 0 " else ""
    val uniqueJourneysSQL =
      """
            select j.from_nomis_agency_id                                     as journey_from_nomis_agency_id,
                   jfl.site_name                                              as journey_from_site_name,
                   jfl.location_type                                          as journey_from_location_type,
                   j.to_nomis_agency_id                                       as journey_to_nomis_agency_id,
                   jtl.site_name                                              as journey_to_site_name,
                   jtl.location_type                                          as journey_to_location_type,
                   COALESCE(pe.price_in_pence, p.price_in_pence)              as unit_price_in_pence,
                   count (CONCAT(j.from_nomis_agency_id, ' ', j.to_nomis_agency_id)) as volume,
                   sum(case when j.billable then COALESCE(pe.price_in_pence, p.price_in_pence) else case when p.price_in_pence is null then null else 0 end end) as total_price_in_pence,
                   sum(case when jfl.site_name is null then 3 else 0 end + case when jtl.site_name is null then 2 else 0 end + case when p.price_in_pence is null then 1 else 0 end) /
                   count (CONCAT(j.from_nomis_agency_id, ' ', j.to_nomis_agency_id)) as null_locations_and_prices_sum
            from MOVES m
                     inner join JOURNEYS j on j.move_id = m.move_id
                     left join LOCATIONS jfl on j.from_nomis_agency_id = jfl.nomis_agency_id
                     left join LOCATIONS jtl on j.to_nomis_agency_id = jtl.nomis_agency_id
                     left join PRICES p on jfl.location_id = p.from_location_id and jtl.location_id = p.to_location_id and j.effective_year = p.effective_year and p.supplier = ?
                     left join PRICE_EXCEPTIONS pe on p.price_id = pe.price_id and pe.month = ?
                     where  m.move_month = ? and m.move_year = ?
                        and m.supplier = ? 
                        and m.move_type is not null
                        and m.drop_off_or_cancelled is not null
            GROUP BY j.from_nomis_agency_id, j.to_nomis_agency_id, jfl.site_name, jtl.site_name, jfl.location_type, jtl.location_type, COALESCE(pe.price_in_pence, p.price_in_pence)
            $havingOnlyUnpriced
            ORDER BY null_locations_and_prices_sum desc, volume desc
      """.trimIndent()

    return jdbcTemplate.query(
      uniqueJourneysSQL,
      journeyWithPricesRowMapper,
      supplier.name,
      startDate.possiblePriceExceptionMonth(),
      startDate.month.value,
      startDate.year,
      supplier.name,
    )
  }

  private fun LocalDate.possiblePriceExceptionMonth() = this.month.value

  fun journeysSummaryInDateRange(
    supplier: Supplier,
    startDate: LocalDate,
    endDateInclusive: LocalDate,
  ): JourneysSummary {
    val journeysSummaryRowMapper = RowMapper { resultSet: ResultSet, _: Int ->
      with(resultSet) {
        JourneysSummary(
          supplier = supplier,
          count = getInt("journey_count"),
          totalPriceInPence = getInt("total_price_in_pence"),
          countWithoutLocations = getInt("count_without_locations"),
          countUnpriced = getInt("count_unpriced"),
        )
      }
    }

    val journeysSummarySQL =
      """
           select count(js.journey) as journey_count, sum(js.price_in_pence) as total_price_in_pence, 
           sum (js.volume_unlocationed) as count_without_locations, sum(js.volume_unpriced) as count_unpriced from(
                select distinct CONCAT(j.from_nomis_agency_id, ' ', j.to_nomis_agency_id) as journey, 
                max(case when jfl.location_id is null or jtl.location_id is null then 1 else 0 end) as volume_unlocationed, 
                sum(case when j.billable then COALESCE(pe.price_in_pence, p.price_in_pence) else 0 end) as price_in_pence, 
                max(case when p.price_in_pence is null then 1 else 0 end) as volume_unpriced 
             from MOVES m 
             inner join JOURNEYS j on j.move_id = m.move_id  
             left join LOCATIONS jfl on j.from_nomis_agency_id = jfl.nomis_agency_id 
             left join LOCATIONS jtl on j.to_nomis_agency_id = jtl.nomis_agency_id 
             left join PRICES p on jfl.location_id = p.from_location_id and jtl.location_id = p.to_location_id and j.effective_year = p.effective_year and p.supplier = ?
             left join PRICE_EXCEPTIONS pe on p.price_id = pe.price_id and pe.month = ?
             where m.move_month = ? and m.move_year = ? and m.supplier = ? and m.move_type is not null and m.drop_off_or_cancelled is not null 
             GROUP BY journey) as js
      """.trimIndent()

    return jdbcTemplate.query(
      journeysSummarySQL,
      journeysSummaryRowMapper,
      supplier.name,
      startDate.possiblePriceExceptionMonth(),
      startDate.month.value,
      startDate.year,
      supplier.name,
    )[0]
  }
}
