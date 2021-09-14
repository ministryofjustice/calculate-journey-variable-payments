package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy
import java.time.Month
import java.time.Year

@PageUrl("http://localhost:8080/select-month")
class SelectMonthYearPage : ApplicationPage() {

  @FindBy(id = "month-year")
  private lateinit var monthYearField: FluentWebElement

  @FindBy(id = "submit-month-year")
  private lateinit var goToMonthYearButton: FluentWebElement

  fun navigateToDashboardFor(month: Month, year: Year) {
    this.monthYearField.fill().withText("${month.name} ${year.value}")
    goToMonthYearButton.submit()
  }
}
