package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound

import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SupplierPrices
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneyWithPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovesSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JourneyService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveTypeSummaries
import java.io.FileInputStream
import java.time.LocalDateTime
import java.util.UUID
import java.util.stream.Stream

internal class PricesSpreadsheetGeneratorTest {
  private val timeSource = TimeSource { LocalDateTime.of(2020, 11, 18, 0, 0) }

  private fun createMoveList(id: Int, moveType: MoveType) = listOf(
    Move(
      "Move$id",
      null,
      timeSource.dateTime(),
      Supplier.GEOAMEY,
      moveType,
      MoveStatus.completed,
      "$moveType", // this is used to verify the correct move type is put into the correct spreadsheet tab
      timeSource.date(),
      "FROM$id",
      "PR"
    )
  )

  private val moveService: MoveService = mock {
    on { it.moves(any(), any()) } doReturn mapOf(
      MoveType.STANDARD to createMoveList(1, MoveType.STANDARD),
      MoveType.REDIRECTION to createMoveList(2, MoveType.REDIRECTION),
      MoveType.LONG_HAUL to createMoveList(3, MoveType.LONG_HAUL),
      MoveType.LOCKOUT to createMoveList(4, MoveType.LOCKOUT),
      MoveType.MULTI to createMoveList(5, MoveType.MULTI),
      MoveType.CANCELLED to createMoveList(6, MoveType.CANCELLED)
    )
    on { it.moveTypeSummaries(any(), any()) } doReturn MoveTypeSummaries(
      1,
      listOf(MovesSummary(MoveType.STANDARD, 1.0, 1, 0, 12345))
    )
  }
  private val journeyService: JourneyService = mock {
    on { it.distinctJourneysIncludingPriced(any(), any()) } doReturn listOf(
      JourneyWithPrice(
        "FROM1",
        LocationType.PR,
        "From 1",
        "TO1",
        LocationType.AP,
        "To 1",
        1,
        12345,
        12345
      )
    )
  }
  private val locations =
    listOf(Location(LocationType.PR, "LOCATION1", "Location 1"), Location(LocationType.PR, "LOCATION2", "Location 2"))
  private val locationRepository: LocationRepository = mock { on { it.findAll() } doReturn locations }
  private val supplierPrices: SupplierPrices =
    mock {
      on { it.get(any(), any()) } doReturn Stream.of(
        Price(
          UUID.fromString("00000000-0000-0000-0000-000000000000"),
          Supplier.GEOAMEY,
          locations[0],
          locations[1],
          12345,
          timeSource.dateTime(),
          2020
        )
      )
    }

  private val pricesSpreadsheetGenerator = PricesSpreadsheetGenerator(
    timeSource,
    moveService,
    journeyService,
    locationRepository,
    supplierPrices
  )

  @Test
  internal fun `check tabs`() {
    val sheetFile = pricesSpreadsheetGenerator.generate(Supplier.GEOAMEY, timeSource.date())
    val workbook = XSSFWorkbook(FileInputStream(sheetFile))

    mapOf(
      "Summary" to null,
      "Standard" to MoveType.STANDARD.toString(),
      "Lockouts" to MoveType.LOCKOUT.toString(),
      "Long haul" to MoveType.LONG_HAUL.toString(),
      "Redirections" to MoveType.REDIRECTION.toString(),
      "Multi-type" to MoveType.MULTI.toString(),
      "Cancelled" to MoveType.CANCELLED.toString(),
      "Journeys" to null,
      "JPC Price book" to null,
      "Locations" to null
    ).forEach { (sheetName, maybeMoveRef) ->
      val sheet = workbook.getSheet(sheetName)

      assertThat(sheet).isNotNull

      maybeMoveRef?.let { expectedMoveReference ->
        assertThat(sheet.firstRowOfData().moveReference()).isEqualTo(expectedMoveReference)
      }
    }
  }

  private fun XSSFSheet.firstRowOfData() = this.getRow(9)

  private fun XSSFRow.moveReference() = this.getCell(0).toString()
}
