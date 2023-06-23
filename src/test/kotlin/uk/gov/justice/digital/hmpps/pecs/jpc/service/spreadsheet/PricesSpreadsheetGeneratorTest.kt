package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SupplierPrices
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyWithPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovesSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceExceptionRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.JourneyService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.MoveTypeSummaries
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
      MoveStatus.Completed,
      "$moveType", // this is used to verify the correct move type is put into the correct spreadsheet tab
      timeSource.date(),
      "FROM$id",
      "PR",
    ),
  )

  private val moveService: MoveService = mock {
    on {
      movesForMoveType(any(), eq(MoveType.STANDARD), any())
    } doReturn createMoveList(1, MoveType.STANDARD)
    on {
      movesForMoveType(any(), eq(MoveType.REDIRECTION), any())
    } doReturn createMoveList(2, MoveType.REDIRECTION)
    on {
      movesForMoveType(any(), eq(MoveType.LONG_HAUL), any())
    } doReturn createMoveList(3, MoveType.LONG_HAUL)
    on {
      movesForMoveType(any(), eq(MoveType.LOCKOUT), any())
    } doReturn createMoveList(4, MoveType.LOCKOUT)
    on {
      movesForMoveType(any(), eq(MoveType.MULTI), any())
    } doReturn createMoveList(5, MoveType.MULTI)
    on {
      movesForMoveType(any(), eq(MoveType.CANCELLED), any())
    } doReturn createMoveList(6, MoveType.CANCELLED)
    on { it.moveTypeSummaries(any(), any()) } doReturn MoveTypeSummaries(
      1,
      listOf(MovesSummary(MoveType.STANDARD, 1.0, 1, 0, 12345)),
    )
    on { it.candidateReconciliations(any(), any()) } doReturn createMoveList(7, MoveType.STANDARD)
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
        12345,
      ),
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
          2020,
        ),
      )
    }

  private val priceExceptionRepository: PriceExceptionRepository = mock()

  private val pricesSpreadsheetGenerator = PricesSpreadsheetGenerator(
    timeSource,
    moveService,
    journeyService,
    locationRepository,
    supplierPrices,
    priceExceptionRepository,
    true,
  )

  @Test
  internal fun `check reconciliation tab feature not present`() {
    val generatorWithoutReconciliationMoves = PricesSpreadsheetGenerator(
      timeSource,
      moveService,
      journeyService,
      locationRepository,
      supplierPrices,
      priceExceptionRepository,
    )

    val workbook = XSSFWorkbook(generatorWithoutReconciliationMoves.generate(Supplier.GEOAMEY, timeSource.date()))

    assertThat(workbook.getSheet("Reconciliation Moves")).isNull()
  }

  @Test
  internal fun `check reconciliation tab feature is present`() {
    val workbook = XSSFWorkbook(pricesSpreadsheetGenerator.generate(Supplier.GEOAMEY, timeSource.date()))

    assertThat(workbook.getSheet("Reconciliation Moves")).isNotNull
  }

  @Test
  internal fun `check all tabs`() {
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
      "Locations" to null,
      "Reconciliation Moves" to MoveType.STANDARD.toString(),
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
