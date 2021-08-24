package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import kotlin.reflect.KClass

sealed class Pages<T : ApplicationPage>(clazz: KClass<T>) {

  val page: Class<T> = clazz.java

  object AddPrice : Pages<AddPricePage>(AddPricePage::class)

  object ChooseSupplier : Pages<ChooseSupplierPage>(ChooseSupplierPage::class)

  object Dashboard : Pages<DashboardPage>(DashboardPage::class)

  object FindMove : Pages<FindMovePage>(FindMovePage::class)

  object JourneysForReview : Pages<JourneysForReviewPage>(JourneysForReviewPage::class)

  object JourneyResults : Pages<JourneyResultsPage>(JourneyResultsPage::class)

  object Login : Pages<LoginPage>(LoginPage::class)

  object ManageJourneyPrice : Pages<ManageJourneyPricePage>(ManageJourneyPricePage::class)

  object ManageJourneyPriceCatalogue : Pages<ManageJourneyPriceCataloguePage>(ManageJourneyPriceCataloguePage::class)

  object ManageLocation : Pages<ManageLocationPage>(ManageLocationPage::class)

  object MapLocation : Pages<MapLocationPage>(MapLocationPage::class)

  object MovesByType : Pages<MovesByTypePage>(MovesByTypePage::class)

  object MoveDetails : Pages<MoveDetailsPage>(MoveDetailsPage::class)

  object SearchLocations : Pages<SearchLocationsPage>(SearchLocationsPage::class)

  object SelectMonthYear : Pages<SelectMonthYearPage>(SelectMonthYearPage::class)

  object UpdatePrice : Pages<UpdatePricePage>(UpdatePricePage::class)
}
