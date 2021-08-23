package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move

@PageUrl("http://localhost:8080/find-move")
class FindMovePage : ApplicationPage() {

  @FindBy(id = "reference")
  private lateinit var referenceInput: FluentWebElement

  @FindBy(id = "find-move")
  private lateinit var findMoveSubmit: FluentWebElement

  fun findBy(move: Move) {
    referenceInput.fill().withText(move.reference)
    findMoveSubmit.submit()
  }
}
