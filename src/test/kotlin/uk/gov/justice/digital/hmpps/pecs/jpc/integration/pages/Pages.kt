package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

sealed class Pages<T : ApplicationPage>(val page: Class<T>) {

  object CHOOSE_SUPPLIER : Pages<ChooseSupplierPage>(ChooseSupplierPage::class.java)

  object DASHBOARD : Pages<DashboardPage>(DashboardPage::class.java)

  object FIND_MOVE : Pages<FindMovePage>(FindMovePage::class.java)

  object JOURNEYS_FOR_REVIEW : Pages<JourneysForReviewPage>(JourneysForReviewPage::class.java)

  object LOGIN : Pages<LoginPage>(LoginPage::class.java)

  object MOVE_DETAILS_PAGE : Pages<MoveDetailsPage>(MoveDetailsPage::class.java)

  object SEARCH_LOCATIONS : Pages<SearchLocationsPage>(SearchLocationsPage::class.java)

  object SELECT_MONTH_YEAR : Pages<SelectMonthYearPage>(SelectMonthYearPage::class.java)
}
