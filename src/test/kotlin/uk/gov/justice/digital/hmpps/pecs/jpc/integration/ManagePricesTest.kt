package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.core.annotation.Page
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.AddPricePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.JourneyResultsPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.JourneysForReviewPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.LoginPage
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
  private lateinit var journeyResultsPage: JourneyResultsPage

  @Page
  private lateinit var updatePricePage: UpdatePricePage

  @Test
  @Order(1)
  fun `missing price is added for GEOAmey journey from Prison One to Prison Two`() {
    goTo(dashboardPage)

    loginPage
      .isAtPage<LoginPage>()
      .login()

    chooseSupplierPage
      .isAtPage<ChooseSupplierPage>()
      .choose(Supplier.GEOAMEY)

    dashboardPage
      .isAtPage<DashboardPage>()
      .isAtMonthYear(LocalDate.now().month, Year.now())
      .navigateToSelectMonthPage()

    selectMonthYearPage
      .isAtPage<SelectMonthYearPage>()
      .navigateToDashboardFor("dec 2020")

    dashboardPage
      .isAtPage<DashboardPage>()
      .isAtMonthYear(Month.DECEMBER, Year.of(2020))
      .navigateToJourneysForReview()

    journeysForReviewPage
      .isAtPage<JourneysForReviewPage>()
      .addPriceForJourney("PRISON1", "PRISON2")

    addPricePage
      .isAtPricePageForJourney("PRISON1", "PRISON2")
      .addPriceForJourney("PRISON1", "PRISON2", Money(1000))

    journeysForReviewPage
      .isAtPage<JourneysForReviewPage>()
      .isPriceAddedMessagePresent("PRISON ONE", "PRISON TWO", Money(1000))
  }

  @Test
  @Order(2)
  fun `price is updated for GEOAmey journey from Prison One to Prison Two`() {
    goTo(dashboardPage)

    loginPage
      .isAtPage<LoginPage>()
      .login()

    chooseSupplierPage
      .isAtPage<ChooseSupplierPage>()
      .choose(Supplier.GEOAMEY)

    dashboardPage
      .isAtPage<DashboardPage>()
      .navigateToManageJourneyPrice()

    manageJourneyPricePage
      .isAtPage<ManageJourneyPricePage>()
      .findJourneyForPricing("PRISON ONE", "PRISON TWO")

    journeyResultsPage
      .isAtResultsPageForJourney("PRISON ONE", "PRISON TWO")
      .isJourneyRowPresent("PRISON ONE", "PRISON TWO", Money(1000))
      .navigateToUpdatePriceFor("PRISON1", "PRISON2")

    updatePricePage
      .isAtPricePageForJourney("PRISON1", "PRISON2")
      .updatePriceForJourney("PRISON1", "PRISON2", Money(2000))

    journeyResultsPage
      .isAtResultsPageForJourney("PRISON ONE", "PRISON TWO")
      .isPriceUpdatedMessagePresent("PRISON ONE", "PRISON TWO", Money(2000))
      .isJourneyRowPresent("PRISON ONE", "PRISON TWO", Money(2000))
  }
}
