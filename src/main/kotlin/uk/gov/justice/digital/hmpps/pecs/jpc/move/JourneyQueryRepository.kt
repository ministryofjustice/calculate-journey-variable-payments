package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate

@Component
class JourneyQueryRepository(@Autowired val jdbcTemplate: JdbcTemplate) {

    fun distinctJourneysBySiteNames(supplier: Supplier, fromSiteName: String?, toSiteName: String?): List<DistinctJourney>{
        val selectDistinctJourneysSQL = """
             select distinct
                   j.from_nomis_agency_id                                     as journey_from_nomis_agency_id,
                   jfl.site_name                                              as journey_from_site_name,
                   jfl.location_type                                          as journey_from_location_type,
                   j.to_nomis_agency_id                                       as journey_to_nomis_agency_id,
                   jtl.site_name                                              as journey_to_site_name,
                   jtl.location_type                                          as journey_to_location_type
            from  JOURNEYS j
                     left join LOCATIONS jfl on j.from_nomis_agency_id = jfl.nomis_agency_id
                     left join LOCATIONS jtl on j.to_nomis_agency_id = jtl.nomis_agency_id
                     left join PRICES p on jfl.location_id = p.from_location_id and jtl.location_id = p.to_location_id
            where j.supplier = ?
        """.trimIndent() +
                (if(fromSiteName.isNullOrBlank()) "" else  " and jfl.site_name = ? ") +
                (if(toSiteName.isNullOrBlank()) "" else  " and jtl.site_name = ? ")


        val distinctJourneysRowMapper = RowMapper { resultSet: ResultSet, _: Int ->
            with(resultSet) {
                DistinctJourney(
                    fromNomisAgencyId = getString("journey_from_nomis_agency_id"),
                    fromLocationType = getString("journey_from_location_type")?.let { LocationType.valueOf(it) },
                    fromSiteName = getString("journey_from_site_name"),
                    toNomisAgencyId = getString("journey_to_nomis_agency_id"),
                    toLocationType = getString("journey_to_location_type")?.let { LocationType.valueOf(it) },
                    toSiteName = getString("journey_to_site_name"),
                )
            }
        }
        val placeholders = listOf(supplier.name, fromSiteName, toSiteName).filter { !it.isNullOrBlank() }.toTypedArray()
        return jdbcTemplate.query(selectDistinctJourneysSQL, placeholders, distinctJourneysRowMapper)
    }

    fun distinctJourneysAndPriceInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate, excludePriced: Boolean = true): List<JourneyWithPrices> {
        val journeyWithPricesRowMapper = RowMapper { resultSet: ResultSet, _: Int ->
            with(resultSet) {
                JourneyWithPrices(
                    fromNomisAgencyId = getString("journey_from_nomis_agency_id"),
                    fromLocationType = getString("journey_from_location_type")?.let { LocationType.valueOf(it) },
                    fromSiteName = getString("journey_from_site_name"),
                    toNomisAgencyId = getString("journey_to_nomis_agency_id"),
                    toLocationType = getString("journey_to_location_type")?.let { LocationType.valueOf(it) },
                    toSiteName = getString("journey_to_site_name"),
                    volume = getInt("volume"),
                    unitPriceInPence = getInt("unit_price_in_pence"),
                    totalPriceInPence = getInt("total_price_in_pence")
                )
            }
        }

        val havingOnlyUnpriced = if(excludePriced) "HAVING max(case when p.price_in_pence is null then 1 else 0 end) > 0 " else ""
        val uniqueJourneysSQL = """
            select j.from_nomis_agency_id                                     as journey_from_nomis_agency_id,
                   jfl.site_name                                              as journey_from_site_name,
                   jfl.location_type                                          as journey_from_location_type,
                   j.to_nomis_agency_id                                       as journey_to_nomis_agency_id,
                   jtl.site_name                                              as journey_to_site_name,
                   jtl.location_type                                          as journey_to_location_type,
                   p.price_in_pence                                           as unit_price_in_pence,
                   count (CONCAT(j.from_nomis_agency_id, ' ', j.to_nomis_agency_id)) as volume,
                   sum(case when j.billable then p.price_in_pence else case when p.price_in_pence is null then null else 0 end end) as total_price_in_pence,
                   sum(case when jfl.site_name is null then 3 else 0 end + case when jtl.site_name is null then 2 else 0 end + case when p.price_in_pence is null then 1 else 0 end) /
                   count (CONCAT(j.from_nomis_agency_id, ' ', j.to_nomis_agency_id)) as null_locations_and_prices_sum
            from MOVES m
                     inner join JOURNEYS j on j.move_id = m.move_id
                     left join LOCATIONS jfl on j.from_nomis_agency_id = jfl.nomis_agency_id
                     left join LOCATIONS jtl on j.to_nomis_agency_id = jtl.nomis_agency_id
                     left join PRICES p on jfl.location_id = p.from_location_id and jtl.location_id = p.to_location_id
                     where m.move_type is not null and m.supplier = ? and m.drop_off_or_cancelled >= ? and m.drop_off_or_cancelled < ?
            GROUP BY j.from_nomis_agency_id, j.to_nomis_agency_id, jfl.site_name, jtl.site_name, jfl.location_type, jtl.location_type, p.price_in_pence
            $havingOnlyUnpriced
            ORDER BY null_locations_and_prices_sum desc, volume desc
        """.trimIndent()

        return jdbcTemplate.query(uniqueJourneysSQL,
                arrayOf(
                    supplier.name,
                    Timestamp.valueOf(startDate.atStartOfDay()),
                    Timestamp.valueOf(endDateInclusive.plusDays(1).atStartOfDay())
                ),
                journeyWithPricesRowMapper)
    }



    fun journeysSummaryInDateRange(supplier: Supplier, startDate: LocalDate, endDateInclusive: LocalDate): JourneysSummary {
        val journeysSummaryRowMapper = RowMapper { resultSet: ResultSet, _: Int ->
            with(resultSet) {
                JourneysSummary(
                    supplier = supplier,
                    count = getInt("journey_count"),
                    totalPriceInPence = getInt("total_price_in_pence"),
                    countWithoutLocations = getInt("count_without_locations"),
                    countUnpriced = getInt("count_unpriced")
                )
            }
        }

        val journeysSummarySQL = "select count(js.journey) as journey_count,\n" +
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
            "             where m.move_type is not null and m.supplier = ? and m.drop_off_or_cancelled >= ? and m.drop_off_or_cancelled < ? " +
            "\n" +
            "    GROUP BY journey) as js"

        return jdbcTemplate.query(journeysSummarySQL,
                arrayOf(
                    supplier.name,
                    Timestamp.valueOf(startDate.atStartOfDay()),
                    Timestamp.valueOf(endDateInclusive.plusDays(1).atStartOfDay())
                ),
                journeysSummaryRowMapper)[0]
    }
}

