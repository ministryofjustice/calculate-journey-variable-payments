package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy

@PageUrl("http://localhost:8080/select-month")
class SelectMonthYearPage : ApplicationPage() {

  @FindBy(id = "month-year")
  private lateinit var monthYearField: FluentWebElement

  @FindBy(id = "submit-month-year")
  private lateinit var goToMonthYearButton: FluentWebElement

  fun navigateToDashboardFor(monthYear: String) {
    this.monthYearField.fill().withText(monthYear)
    goToMonthYearButton.submit()
  }
}
