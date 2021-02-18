package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.move.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDateTime

internal class ImportServiceTest {

  private val timeSourceWithFixedDate: TimeSource = TimeSource { LocalDateTime.of(2021, 2, 18, 12, 0, 0) }
  private val priceImporter: PriceImporter = mock()
  private val reportImporter: ReportImporter = mock()
  private val movePersister: MovePersister = mock()
  private val personPersister: PersonPersister = mock()
  private val importService: ImportService =
    ImportService(timeSourceWithFixedDate, priceImporter, reportImporter, movePersister, personPersister)

  @Test
  internal fun `price importer interactions for Serco`() {
    importService.importPrices(Supplier.SERCO)

    verify(priceImporter).import(Supplier.SERCO)
  }

  @Test
  internal fun `price importer interactions for GEOAmey`() {
    importService.importPrices(Supplier.GEOAMEY)

    verify(priceImporter).import(Supplier.GEOAMEY)
  }

  @Test
  internal fun `report importer interactions`() {
    importService.importReports(timeSourceWithFixedDate.date(), timeSourceWithFixedDate.date())

    verify(reportImporter).importMovesJourneysEvents(timeSourceWithFixedDate.date(), timeSourceWithFixedDate.date())
    verify(reportImporter).importPeople(timeSourceWithFixedDate.date(), timeSourceWithFixedDate.date())
    verify(reportImporter).importProfiles(timeSourceWithFixedDate.date(), timeSourceWithFixedDate.date())
  }
}
