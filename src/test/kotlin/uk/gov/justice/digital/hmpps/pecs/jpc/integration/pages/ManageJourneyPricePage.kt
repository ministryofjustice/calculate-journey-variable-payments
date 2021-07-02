package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl

@PageUrl("http://localhost:8080/search-journeys")
class ManageJourneyPricePage : ApplicationPage() {

  // Note: this is bypassing the actual search journey page due to not liking the JavaScript in the page where the fields are added by the 3rd party JS lib.
  fun findJourneyToManagePrice(fromAgencyId: String, toAgencyId: String) {
    newInstance(UpdatePricePage::class.java).go<UpdatePricePage>("$fromAgencyId-$toAgencyId")
  }
}
