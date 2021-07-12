package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl

@PageUrl("http://localhost:8080/moves/{moveId}")
class MoveDetailsPage : ApplicationPage() {

  /**
   * Moves are actually retrieved by their reference number but the URL contains the move ID.
   */
  fun isAtPageForMoveID(moveRefId: String) = this.isAt(moveRefId).let { this }
}
