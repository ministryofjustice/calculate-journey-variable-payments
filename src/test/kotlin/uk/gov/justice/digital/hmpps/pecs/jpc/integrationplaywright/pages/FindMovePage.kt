package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import java.time.format.DateTimeFormatter

class FindMovePage(page: Page?) {

  private val url = "http://localhost:8080/find-move"
  private val page = page

  fun gotToPage() {
    page?.navigate(url)
  }

  fun isPageSuccessful(): Boolean {
    page?.waitForURL(url)
    return page?.url().equals(url)
  }

  fun findMoveByReferenceId(referenceId: String): Boolean {
    page?.waitForLoadState()
    page?.locator("input#reference")?.fill(referenceId)
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Find Move"))?.click()
    return page?.waitForSelector("h1")?.innerText()?.startsWith("Move: ${referenceId.uppercase()}") == true
  }

  fun assertPageShowsTheMove(move: Move) {
    assert(page?.locator("dd#prison-number")?.innerText().equals(move.person?.prisonNumber))
    assert(page?.locator("dd#first-names")?.innerText().equals(move.person?.firstNames))
    assert(page?.locator("dd#last-name")?.innerText().equals(move.person?.lastName))
    assert(
      page?.locator("dd#date-of-birth")?.innerText()
        .equals(move.person?.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd MM YYYY"))),
    )
    assert(page?.locator("dd#gender")?.innerText().equals(move.person?.gender))
    if (move.pickUpDateTime != null) {
      assert(
        page?.locator("dd#move-pickup-date")?.innerText()
          .equals(move.pickUpDateTime?.format(DateTimeFormatter.ofPattern("dd MMM YYYY, HH:mm"))),
      )
    } else {
      assert(
        page?.locator("dd#move-pickup-date")?.innerText()
          .equals("Not known"),
      )
    }

    assert(
      page?.locator("dd#move-dropoff-date")?.innerText()
        .equals(move.dropOffOrCancelledDateTime?.format(DateTimeFormatter.ofPattern("dd MMM YYYY, HH:mm"))),
    )
    assert(page?.locator("dd#from-site")?.innerText().equals(move.fromSiteName))
    assert(page?.locator("dd#to-site")?.innerText().equals(move.toSiteName))
    assert(page?.locator("dd#move-type")?.innerText().equals(move.moveType?.name))
  }
}
