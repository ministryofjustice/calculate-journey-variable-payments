package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.PageUrl
import org.openqa.selenium.By
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

@PageUrl("http://localhost:8080/moves/{moveId}")
class MoveDetailsPage : ApplicationPage() {

  private val moneyFormatter = DecimalFormat("#,###.00")

  fun isAtPageFor(move: Move, expectedPrice: Money? = null) {
    this.isAt(move.moveId)

    val source = this.pageSource()

    assertThat(source).contains(move.person?.prisonNumber)
    assertThat(source).contains(move.person?.firstNames)
    assertThat(source).contains(move.person?.lastName)
    assertThat(source).contains(move.person?.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd MM yyyy")))
    assertThat(source).contains(move.person?.gender)
    assertThat(source).contains(move.pickUpDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")) ?: "Not known")
    assertThat(source).contains(move.dropOffOrCancelledDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")))
    assertThat(source).contains(move.fromSiteName)
    assertThat(source).contains(move.toSiteName)
    assertThat(source).contains(move.moveType?.name)

    if (expectedPrice != null) {
      val actualPrice = super.find(By.id("move-price")).firstOrNull()

      assertThat(actualPrice).isNotNull

      assertThat(actualPrice!!.text()).isEqualTo("£${moneyFormatter.format(expectedPrice.pounds())}")
    }
  }
}
