package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SupplierPrices
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyWithPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovesSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JourneyService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveTypeSummaries
import java.io.FileInputStream
import java.time.LocalDateTime
import java.util.UUID
import java.util.stream.Stream

@SpringJUnitConfig(TestConfig::class)
internal class PricesSpreadsheetGeneratorTest(@Autowired private val template: JPCTemplateProvider) {
  private val timeSource = TimeSource { LocalDateTime.of(2020, 11, 18, 0, 0) }

  private fun createMoveList(id: Int, moveType: MoveType) = listOf(
    Move(
      "MOVE$id",
      null,
      timeSource.dateTime(),
      Supplier.GEOAMEY,
      moveType,
      MoveStatus.completed,
      "Move$id",
      timeSource.date(),
      "FROM$id",
      "PR"
    )
  )

  private val moveService: MoveService = mock {
    on { it.moves(any(), any()) } doReturn listOf(
      createMoveList(1, MoveType.STANDARD),
      createMoveList(2, MoveType.REDIRECTION),
      createMoveList(3, MoveType.LONG_HAUL),
      createMoveList(4, MoveType.LOCKOUT),
      createMoveList(5, MoveType.MULTI),
      createMoveList(6, MoveType.CANCELLED)
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
    template,
    timeSource,
    moveService,
    journeyService,
    locationRepository,
    supplierPrices
  )

  @Test
  internal fun `check tabs`() {
    val sheetFile = pricesSpreadsheetGenerator.generate(Supplier.GEOAMEY, timeSource.date())
    val sheet = XSSFWorkbook(FileInputStream(sheetFile))

    listOf(
      "Summary",
      "Standard",
      "Lockouts",
      "Long haul",
      "Redirections",
      "Multi-type",
      "Cancelled",
      "Journeys",
      "JPC Price book",
      "Locations"
    ).forEachIndexed { index, s -> assertThat(sheet.getSheetName(index) == s) }
  }
}
