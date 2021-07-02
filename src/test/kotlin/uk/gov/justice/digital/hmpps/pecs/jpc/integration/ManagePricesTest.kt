package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.core.annotation.Page
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.AddPricePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.JourneysForReviewPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ManageJourneyPricePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SelectMonthYearPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.UpdatePricePage
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.Month
import java.time.Year

@TestMethodOrder(OrderAnnotation::class)
internal class ManagePricesTest : IntegrationTest() {

  @Page
  private lateinit var dashboardPage: DashboardPage

  @Page
  private lateinit var chooseSupplierPage: ChooseSupplierPage

  @Page
  private lateinit var selectMonthYearPage: SelectMonthYearPage

  @Page
  private lateinit var journeysForReviewPage: JourneysForReviewPage

  @Page
  private lateinit var addPricePage: AddPricePage

  @Page
  private lateinit var manageJourneyPricePage: ManageJourneyPricePage

  @Page
  private lateinit var updatePricePage: UpdatePricePage

  @Test
  @Order(1)
  fun `add price`() {
    goTo(dashboardPage)
    loginPage.isAt()
    loginPage.login()

    chooseSupplierPage.isAt()
    chooseSupplierPage.choose(Supplier.GEOAMEY)

    dashboardPage.isAt()
    dashboardPage.isAtMonthYear(LocalDate.now().month, Year.now())
    dashboardPage.navigateToSelectMonthPage()

    selectMonthYearPage.isAt()
    selectMonthYearPage.navigateToDashboardFor("dec 2020")

    dashboardPage.isAt()
    dashboardPage.isAtMonthYear(Month.DECEMBER, Year.of(2020))
    dashboardPage.navigateToJourneysForReview()

    journeysForReviewPage.isAt()
    journeysForReviewPage.addPriceForJourney("PRISON1", "PRISON2")

    addPricePage.isAtPricePageForJourney("PRISON1", "PRISON2")
    addPricePage.addPriceForJourney("PRISON1", "PRISON2", Money(1000))
  }

  @Test
  @Order(2)
  fun `update price`() {
    goTo(dashboardPage)
    loginPage.isAt()
    loginPage.login()

    chooseSupplierPage.isAt()
    chooseSupplierPage.choose(Supplier.GEOAMEY)

    dashboardPage.isAt()
    dashboardPage.navigateToManageJourneyPrice()

    manageJourneyPricePage.isAt()
    manageJourneyPricePage.findJourneyToManagePrice("PRISON1", "PRISON2")

    updatePricePage.isAtPricePageForJourney("PRISON1", "PRISON2")
    updatePricePage.updatePriceForJourney("PRISON1", "PRISON2", Money(2000))
  }
}
