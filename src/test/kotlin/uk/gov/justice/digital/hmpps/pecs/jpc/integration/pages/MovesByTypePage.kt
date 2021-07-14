package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.openqa.selenium.By
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType

@PageUrl("http://localhost:8080/moves-by-type/{moveType}")
class MovesByTypePage : ApplicationPage() {

  fun isAtPageFor(moveType: MoveType): MovesByTypePage {
    isAt(moveType.name)

    return this
  }

  fun navigateToDetailsFor(move: Move) {
    find(By.linkText(move.reference)).click()
  }
}
