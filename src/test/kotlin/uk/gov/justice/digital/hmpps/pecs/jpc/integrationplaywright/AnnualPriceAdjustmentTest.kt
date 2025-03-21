package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.AnnualPriceAdjustmentPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ManageJourneyPriceCataloguePage
import kotlin.random.Random

internal class AnnualPriceAdjustmentTest : PlayWrightTest() {

  @Test
  fun `Apply Inflationary rate price adjustments for SERCO`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    val manageJourneyPriceCatalogue = ManageJourneyPriceCataloguePage(page)
    val annualPriceAdjustmentPage = AnnualPriceAdjustmentPage(page)

    loginPage.login()
    supplierPage.gotToPage()
    assert(supplierPage.isPageSuccessful())
    supplierPage.goToSercoDashboard()
    assert(dashboardPage.isPageSuccessful(Supplier.SERCO))
    manageJourneyPriceCatalogue.gotToPage()
    assert(manageJourneyPriceCatalogue.isPageSuccessful())
    manageJourneyPriceCatalogue.goToAnnualPriceAdjustment()
    val rate = Random.nextDouble(0.01, 0.99)
    val details = "Inflationary rate of $rate."
    annualPriceAdjustmentPage.applyBulkPriceAdjustment(rate, null, details)
    annualPriceAdjustmentPage.showPriceAdjustmentHistoryTab()
    assert(annualPriceAdjustmentPage.isPriceAdjustmentRecordsPresent(details))
  }

  @Test
  fun `Apply Inflationary and Volumetric rate price adjustments for SERCO`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    val manageJourneyPriceCatalogue = ManageJourneyPriceCataloguePage(page)
    val annualPriceAdjustmentPage = AnnualPriceAdjustmentPage(page)

    loginPage.login()
    supplierPage.gotToPage()
    assert(supplierPage.isPageSuccessful())
    supplierPage.goToSercoDashboard()
    assert(dashboardPage.isPageSuccessful(Supplier.SERCO))
    manageJourneyPriceCatalogue.gotToPage()
    assert(manageJourneyPriceCatalogue.isPageSuccessful())
    manageJourneyPriceCatalogue.goToAnnualPriceAdjustment()
    val inflationaryRate = Random.nextDouble(0.01, 0.99)
    val volumetricRate = Random.nextDouble(1.0, 9.0)
    val details = "Inflationary rate of $inflationaryRate and Volumetric rate of $volumetricRate."
    annualPriceAdjustmentPage.applyBulkPriceAdjustment(inflationaryRate, volumetricRate, details)
    annualPriceAdjustmentPage.showPriceAdjustmentHistoryTab()
    assert(annualPriceAdjustmentPage.isPriceAdjustmentRecordsPresent(details))
  }

  @Test
  fun `Apply Inflationary rate price adjustments for GEOAMEY`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    val manageJourneyPriceCatalogue = ManageJourneyPriceCataloguePage(page)
    val annualPriceAdjustmentPage = AnnualPriceAdjustmentPage(page)

    loginPage.login()
    supplierPage.gotToPage()
    assert(supplierPage.isPageSuccessful())
    supplierPage.goToGeoameyDashboard()
    assert(dashboardPage.isPageSuccessful(Supplier.GEOAMEY))
    manageJourneyPriceCatalogue.gotToPage()
    assert(manageJourneyPriceCatalogue.isPageSuccessful())
    manageJourneyPriceCatalogue.goToAnnualPriceAdjustment()
    val rate = Random.nextDouble(0.01, 0.99)
    val details = "Inflationary rate of $rate."
    annualPriceAdjustmentPage.applyBulkPriceAdjustment(rate, null, details)
    annualPriceAdjustmentPage.showPriceAdjustmentHistoryTab()
    assert(annualPriceAdjustmentPage.isPriceAdjustmentRecordsPresent(details))
  }

  @Test
  fun `Apply Inflationary and Volumetric rate price adjustments for GEOAMEY`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    val manageJourneyPriceCatalogue = ManageJourneyPriceCataloguePage(page)
    val annualPriceAdjustmentPage = AnnualPriceAdjustmentPage(page)

    loginPage.login()
    supplierPage.gotToPage()
    assert(supplierPage.isPageSuccessful())
    supplierPage.goToGeoameyDashboard()
    assert(dashboardPage.isPageSuccessful(Supplier.GEOAMEY))
    manageJourneyPriceCatalogue.gotToPage()
    assert(manageJourneyPriceCatalogue.isPageSuccessful())
    manageJourneyPriceCatalogue.goToAnnualPriceAdjustment()
    val inflationaryRate = Random.nextDouble(0.01, 0.99)
    val volumetricRate = Random.nextDouble(1.0, 9.0)
    val details = "Inflationary rate of $inflationaryRate and Volumetric rate of $volumetricRate."
    annualPriceAdjustmentPage.applyBulkPriceAdjustment(inflationaryRate, volumetricRate, details)
    annualPriceAdjustmentPage.showPriceAdjustmentHistoryTab()
    assert(annualPriceAdjustmentPage.isPriceAdjustmentRecordsPresent(details))
  }
}
