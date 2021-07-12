package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy

@PageUrl("http://localhost:8080/find-move")
class FindMovePage : ApplicationPage() {

  @FindBy(id = "reference")
  private lateinit var referenceInput: FluentWebElement

  @FindBy(id = "find-move")
  private lateinit var findMoveSubmit: FluentWebElement

  fun findMoveByReferenceId(moveReferenceId: String) {
    referenceInput.fill().withText(moveReferenceId)
    findMoveSubmit.submit()
  }
}
