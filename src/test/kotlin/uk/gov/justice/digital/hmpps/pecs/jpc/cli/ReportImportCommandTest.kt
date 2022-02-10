package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportReportsService
import java.time.LocalDate

internal class ReportImportCommandTest {

  private val importReportsService: ImportReportsService = mock()

  private val date: LocalDate = LocalDate.of(2020, 9, 30)

  private val commands: ReportImportCommand = ReportImportCommand(importReportsService)

  @Test
  internal fun `given a date range the import service is called with the expected dates`() {
    commands.importReports(date, date.plusDays(1))

    verify(importReportsService).importAllReportsOn(date)
    verify(importReportsService).importAllReportsOn(date.plusDays(1))
  }

  @Test
  internal fun `given a date in the past the import people and profiles is called with the correct date`() {
    commands.importPeopleAndProfiles(date)

    verify(importReportsService).importPeopleProfileReportsStartingFrom(date)
  }
}
