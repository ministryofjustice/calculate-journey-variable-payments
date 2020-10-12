package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JCPTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.*
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class RedirectionMovesSheetTest(@Autowired private val template: JCPTemplateProvider) {

    private val workbook: Workbook = XSSFWorkbook(template.get())

    @Test
    internal fun `test redirection prices`() {
        val movesDate = LocalDate.of(2020, 9, 10)

        val journey1WithEvents = JourneyWithEvents(journeyFactory(journeyId = "J1", billable = true), listOf(
                journeyEventFactory(),
                journeyEventFactory(type = EventType.JOURNEY_COMPLETE.value)
        ))
        val journey2WithEvents = JourneyWithEvents(journeyFactory(journeyId = "J2", billable = true), listOf())

        val redirectMove = Report(
                move = moveFactory(),
                person = personFactory(),
                events = listOf(
                        moveEventFactory(type = EventType.MOVE_START.value, notes = "started", occurredAt = movesDate.atStartOfDay().plusHours(5)),
                        moveEventFactory(type = EventType.MOVE_REDIRECT.value, notes = "redirected again", occurredAt = movesDate.atStartOfDay().plusHours(7)),
                        moveEventFactory(type = EventType.MOVE_COMPLETE.value, notes = "completed", occurredAt = movesDate.atStartOfDay().plusHours(10))
                ),
                journeysWithEvents = listOf(journey1WithEvents, journey2WithEvents)
        )

        val fromLocation = fromLocationFactory()
        val toLocation = toLocationFactory()
        val redirectPrice = MovePrice(redirectMove, listOf(
                JourneyPrice(journey1WithEvents, 1001),
                JourneyPrice(journey2WithEvents, 1001)

        ))

        val sheet = RedirectionMovesSheet(workbook, PriceSheet.Header(movesDate, ClosedRangeLocalDate(movesDate, movesDate), Supplier.SERCO))
        sheet.writeMoves(listOf(redirectPrice))

        assertCellEquals(sheet, 10, 0, redirectMove.move.reference)
        assertCellEquals(sheet, 10, 1, fromLocation.siteName)
        assertCellEquals(sheet, 10, 2, fromLocation.locationType.name) // pick up location type

        assertCellEquals(sheet, 10, 3, toLocation.siteName)
        assertCellEquals(sheet, 10, 4, toLocation.locationType.name) // drop off location type

        assertCellEquals(sheet, 10, 5, "10/09/2020") // Pick up date
        assertCellEquals(sheet, 10, 6, "05:00") // Pick up time
        assertCellEquals(sheet, 10, 7, "10/09/2020") // Drop off date
        assertCellEquals(sheet, 10, 8, "10:00") // Drop off time

        assertCellEquals(sheet, 10, 9, journey1WithEvents.journey.vehicleRegistration + ", " + journey2WithEvents.journey.vehicleRegistration)

        assertCellEquals(sheet, 10, 10, redirectMove.person?.prisonNumber)

        assertCellEquals(sheet, 10, 11, 20.02) // price

        assertCellEquals(sheet, 10, 12, "") // billable shouldn't be shown
        assertCellEquals(sheet, 10, 13, "MoveRedirect: redirected again") // should only show the redirect event notes


        // Journey 1
        assertCellEquals(sheet, 11, 0, "Journey 1")
        with(journey1WithEvents.journey) {
            assertCellEquals(sheet, 11, 1, fromLocation.siteName)
            assertCellEquals(sheet, 11, 2, fromLocation.locationType.name)
            assertCellEquals(sheet, 11, 3, toLocation.siteName)
            assertCellEquals(sheet, 11, 4, toLocation.locationType.name)
        }
        assertCellEquals(sheet, 11, 5, "16/06/2020") // Pick up date
        assertCellEquals(sheet, 11, 6, "10:20") // Pick up time
        assertCellEquals(sheet, 11, 7, "16/06/2020") // Drop off date
        assertCellEquals(sheet, 11, 8, "10:20") // Drop off time

        assertCellEquals(sheet, 11, 9, journey1WithEvents.journey.vehicleRegistration)
        assertCellEquals(sheet, 11, 10, "") // no prison number for journeys

        assertCellEquals(sheet, 11, 11, 10.01) // price

        assertCellEquals(sheet, 11, 12, "YES") // contractor billable

        assertCellEquals(sheet, 11, 13, "") // no notes

        // Journey 2
        assertCellEquals(sheet, 12, 0, "Journey 2")
    }
}