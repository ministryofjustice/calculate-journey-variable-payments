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
    supplierPage.isPageSuccessful()
    supplierPage.goToSercoDashboard()
    dashboardPage.isPageSuccessful(Supplier.SERCO)
    manageJourneyPriceCatalogue.gotToPage()
    manageJourneyPriceCatalogue.isPageSuccessful()
    manageJourneyPriceCatalogue.goToAnnualPriceAdjustment()
    val rate = String.format("%.2f", Random.nextDouble(0.01, 0.99))
    val details = "Inflationary rate of $rate."
    val formSubmitted = annualPriceAdjustmentPage.applyBulkPriceAdjustment(rate, null, details)
    assert(formSubmitted) { "Validation error occurred for $details" }
    annualPriceAdjustmentPage.showPriceAdjustmentHistoryTab()
    annualPriceAdjustmentPage.isPriceAdjustmentRecordsPresent(details)
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
    supplierPage.isPageSuccessful()
    supplierPage.goToSercoDashboard()
    dashboardPage.isPageSuccessful(Supplier.SERCO)
    manageJourneyPriceCatalogue.gotToPage()
    manageJourneyPriceCatalogue.isPageSuccessful()
    manageJourneyPriceCatalogue.goToAnnualPriceAdjustment()
    val inflationaryRate = String.format("%.2f", Random.nextDouble(0.01, 0.99))
    val volumetricRate = String.format("%.2f", Random.nextDouble(1.0, 9.0))
    val details = "Inflationary rate of $inflationaryRate and Volumetric rate of $volumetricRate."
    val formSubmitted = annualPriceAdjustmentPage.applyBulkPriceAdjustment(inflationaryRate, volumetricRate, details)
    assert(formSubmitted) { "Validation error occurred for $details" }
    annualPriceAdjustmentPage.showPriceAdjustmentHistoryTab()
    annualPriceAdjustmentPage.isPriceAdjustmentRecordsPresent(details)
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
    supplierPage.isPageSuccessful()
    supplierPage.goToGeoameyDashboard()
    dashboardPage.isPageSuccessful(Supplier.GEOAMEY)
    manageJourneyPriceCatalogue.gotToPage()
    manageJourneyPriceCatalogue.isPageSuccessful()
    manageJourneyPriceCatalogue.goToAnnualPriceAdjustment()
    val rate = String.format("%.2f", Random.nextDouble(0.01, 0.99))
    val details = "Inflationary rate of $rate."
    val formSubmitted = annualPriceAdjustmentPage.applyBulkPriceAdjustment(rate, null, details)
    assert(formSubmitted) { "Validation error occurred for $details" }
    annualPriceAdjustmentPage.showPriceAdjustmentHistoryTab()
    annualPriceAdjustmentPage.isPriceAdjustmentRecordsPresent(details)
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
    supplierPage.isPageSuccessful()
    supplierPage.goToGeoameyDashboard()
    dashboardPage.isPageSuccessful(Supplier.GEOAMEY)
    manageJourneyPriceCatalogue.gotToPage()
    manageJourneyPriceCatalogue.isPageSuccessful()
    manageJourneyPriceCatalogue.goToAnnualPriceAdjustment()
    val inflationaryRate = String.format("%.2f", Random.nextDouble(0.01, 0.99))
    val volumetricRate = String.format("%.2f", Random.nextDouble(1.0, 9.0))
    val details = "Inflationary rate of $inflationaryRate and Volumetric rate of $volumetricRate."
    val formSubmitted = annualPriceAdjustmentPage.applyBulkPriceAdjustment(inflationaryRate, volumetricRate, details)
    assert(formSubmitted) { "Validation error occurred for $details" }
    annualPriceAdjustmentPage.showPriceAdjustmentHistoryTab()
    annualPriceAdjustmentPage.isPriceAdjustmentRecordsPresent(details)
  }
}
