package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

@PageUrl("http://localhost:8080/moves/{moveId}")
class MoveDetailsPage : ApplicationPage() {

  @FindBy(id = "prison-number")
  private lateinit var prisonNumber: FluentWebElement

  @FindBy(id = "first-names")
  private lateinit var firstNames: FluentWebElement

  @FindBy(id = "last-name")
  private lateinit var lastName: FluentWebElement

  @FindBy(id = "date-of-birth")
  private lateinit var dateOfBirth: FluentWebElement

  @FindBy(id = "gender")
  private lateinit var gender: FluentWebElement

  @FindBy(id = "move-pickup-date")
  private lateinit var movePickupDate: FluentWebElement

  @FindBy(id = "move-dropoff-date")
  private lateinit var moveDropoffDate: FluentWebElement

  @FindBy(id = "from-site")
  private lateinit var fromSite: FluentWebElement

  @FindBy(id = "to-site")
  private lateinit var toSite: FluentWebElement

  @FindBy(id = "move-type")
  private lateinit var moveType: FluentWebElement

  @FindBy(id = "move-price")
  private lateinit var movePrice: FluentWebElement

  private val moneyFormatter = DecimalFormat("#,###.00")

  fun isAtPageFor(move: Move, expectedPrice: Money? = null) {
    this.isAt(move.moveId)

    assertThat(prisonNumber.text()).contains(move.person?.prisonNumber)
    assertThat(firstNames.text()).contains(move.person?.firstNames)
    assertThat(lastName.text()).contains(move.person?.lastName)
    assertThat(dateOfBirth.text()).contains(move.person?.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd MM yyyy")))
    assertThat(gender.text()).contains(move.person?.gender)
    assertThat(movePickupDate.text()).contains(move.pickUpDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")) ?: "Not known")
    assertThat(moveDropoffDate.text()).contains(move.dropOffOrCancelledDateTime?.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")))
    assertThat(fromSite.text()).contains(move.fromSiteName)
    assertThat(toSite.text()).contains(move.toSiteName)
    assertThat(moveType.text()).contains(move.moveType?.name)

    if (expectedPrice != null) {
      assertThat(movePrice.text()).isEqualTo("£${moneyFormatter.format(expectedPrice.pounds())}")
    }
  }
}
