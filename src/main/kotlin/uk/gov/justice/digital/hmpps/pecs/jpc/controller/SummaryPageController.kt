package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.constraints.ValidMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.JourneyService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.MonthYearParser
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * This is where the user is directed to upon logging into the application. If they haven't chosen the supplier they
 * will be redirected to the 'choose supplier' screen and then directed back to here.
 */
data class MonthsWidget(val currentMonth: LocalDate, val nextMonth: LocalDate, val previousMonth: LocalDate)

private val logger = loggerFor<SummaryPageController>()

@Controller
@SessionAttributes(
  SUPPLIER_ATTRIBUTE,
  DATE_ATTRIBUTE,
  START_OF_MONTH_DATE_ATTRIBUTE,
  END_OF_MONTH_DATE_ATTRIBUTE,
)
class SummaryPageController(
  @Autowired val moveService: MoveService,
  @Autowired val journeyService: JourneyService,
  @Autowired val timeSource: TimeSource,
  @Autowired val actualEffectiveYear: EffectiveYear,
) : PrimaryNavigationBar {

  override fun primaryNavigationChoice() = PrimaryNavigation.SUMMARY

  @RequestMapping("/")
  fun homepage(model: ModelMap): RedirectView {
    logger.info("redirecting to dashboard")

    return RedirectView(DASHBOARD_URL)
  }

  @RequestMapping("/choose-supplier")
  fun chooseSupplier(model: ModelMap): String {
    return "choose-supplier"
  }

  @RequestMapping("/choose-supplier/serco")
  fun chooseSupplierSerco(model: ModelMap): RedirectView {
    logger.info("chosen supplier Serco")

    model.apply {
      addAttribute(SUPPLIER_ATTRIBUTE, Supplier.SERCO)
      addAttribute(DATE_ATTRIBUTE, timeSource.startOfMonth())
    }

    return RedirectView(DASHBOARD_URL)
  }

  @RequestMapping("/choose-supplier/geoamey")
  fun chooseSupplierGeoAmey(model: ModelMap): RedirectView {
    logger.info("chosen supplier GEOAmey")

    model.apply {
      addAttribute(SUPPLIER_ATTRIBUTE, Supplier.GEOAMEY)
      addAttribute(DATE_ATTRIBUTE, timeSource.startOfMonth())
    }

    return RedirectView(DASHBOARD_URL)
  }

  private fun TimeSource.startOfMonth() = this.date().withDayOfMonth(1)

  @RequestMapping("$MOVES_BY_TYPE_URL/{moveTypeString}")
  fun movesByType(
    @PathVariable moveTypeString: String,
    @ModelAttribute(name = START_OF_MONTH_DATE_ATTRIBUTE)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    startOfMonth: LocalDate,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    model: ModelMap,
  ): String {
    logger.info("moves by type $moveTypeString")

    val moveType = MoveType.valueOfCaseInsensitive(moveTypeString)
    val moves = moveService.movesForMoveType(supplier, moveType, startOfMonth)
    val moveTypeSummary = moveService.summaryForMoveType(supplier, moveType, startOfMonth)

    model.apply {
      addAttribute(
        "months",
        MonthsWidget(
          (startOfMonth),
          nextMonth = (startOfMonth.plusMonths(1)),
          previousMonth = (startOfMonth.minusMonths(1)),
        ),
      )
      addAttribute("summary", moveTypeSummary.movesSummary)
      addAttribute("moves", moves)
      addAttribute("moveType", moveType.label)
    }

    return "moves-by-type"
  }

  @RequestMapping("$MOVES_URL/{moveId}")
  fun moves(
    @PathVariable moveId: String,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    model: ModelMap,
  ): ModelAndView {
    logger.info("$supplier move $moveId")

    val maybeMove = moveService.moveWithPersonJourneysAndEvents(moveId, supplier, model.getStartOfMonth().month)

    return maybeMove?.let {
      model.addAttribute(MOVE_ATTRIBUTE, maybeMove)
      model.addAttribute(START_OF_MONTH_DATE_ATTRIBUTE, it.pickUpDateTime?.atStartOfMonthForBreadcrumbTrail())
      ModelAndView("move")
    } ?: ModelAndView("error/404", HttpStatus.NOT_FOUND)
  }

  private fun LocalDateTime.atStartOfMonthForBreadcrumbTrail() = this.toLocalDate()?.withDayOfMonth(1)

  @RequestMapping(JOURNEYS_URL)
  fun journeys(
    @ModelAttribute(name = DATE_ATTRIBUTE)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    startOfMonth: LocalDate,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    @ModelAttribute(name = "flashAttrMappedLocationName") locationName: String?,
    @ModelAttribute(name = "flashAttrMappedAgencyId") agencyId: String?,
    model: ModelMap,
  ): String {
    logger.info("journeys for review for $supplier")

    removeAttributesIf(locationName.isNullOrEmpty(), model, "flashAttrMappedLocationName", "flashAttrMappedAgencyId")

    model.apply {
      addAttribute("journeysSummary", journeyService.journeysSummary(supplier, startOfMonth))
      addAttribute("journeys", journeyService.distinctJourneysExcludingPriced(supplier, startOfMonth))
      addAttribute("canAddPrice", actualEffectiveYear.canAddOrUpdatePrices(model.getSelectedEffectiveYear()))
    }

    return "journeys"
  }

  private fun removeAttributesIf(condition: Boolean, model: ModelMap, vararg attributeNames: String) {
    if (condition) attributeNames.forEach { model.remove(it) }
  }

  @RequestMapping(DASHBOARD_URL)
  fun dashboard(
    @RequestParam(
      name = DATE_ATTRIBUTE,
      required = false,
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    requestParamStartOfMonth: LocalDate?,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    model: ModelMap,
  ): Any {
    logger.info("dashboard for $supplier")

    model.apply {
      requestParamStartOfMonth?.let { addAttribute(DATE_ATTRIBUTE, it) }

      val startOfMonth = getStartOfMonth()
      val endOfMonth = getEndOfMonth()

      addAttribute(START_OF_MONTH_DATE_ATTRIBUTE, startOfMonth)
      addAttribute(END_OF_MONTH_DATE_ATTRIBUTE, endOfMonth)

      val countAndSummaries = moveService.moveTypeSummaries(supplier, startOfMonth)
      val journeysSummary = journeyService.journeysSummary(supplier, startOfMonth)

      addAttribute(
        "months",
        MonthsWidget(
          (startOfMonth),
          nextMonth = (startOfMonth.plusMonths(1)),
          previousMonth = (startOfMonth.minusMonths(1)),
        ),
      )
      addAttribute("summary", countAndSummaries.summary())
      addAttribute("journeysSummary", journeysSummary)
      addAttribute("summaries", countAndSummaries.allSummaries())
    }
    return "dashboard"
  }

  @GetMapping(SELECT_MONTH_URL)
  fun selectMonth(@ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier, model: ModelMap): Any {
    logger.info("select month for $supplier")

    model.addAttribute("form", JumpToMonthForm(date = ""))
    return "select-month"
  }

  data class JumpToMonthForm(@ValidMonthYear val date: String)

  @PostMapping(SELECT_MONTH_URL)
  fun jumpToMonth(
    @Valid
    @ModelAttribute("form")
    form: JumpToMonthForm,
    result: BindingResult,
    model: ModelMap,
  ): Any {
    if (result.hasErrors()) {
      return "select-month"
    }

    logger.info("selected month ${MonthYearParser.atStartOf(form.date)}")

    model.addAttribute(DATE_ATTRIBUTE, MonthYearParser.atStartOf(form.date))

    return RedirectView(DASHBOARD_URL)
  }

  data class FindMoveForm(val reference: String = "")

  @GetMapping(FIND_MOVE_URL)
  fun findMove(model: ModelMap): Any {
    model.addAttribute("form", FindMoveForm())
    return "find-move"
  }

  @PostMapping(FIND_MOVE_URL)
  fun performFindMove(
    @Valid
    @ModelAttribute("form")
    form: FindMoveForm,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    result: BindingResult,
    model: ModelMap,
    redirectAttributes: RedirectAttributes,
  ): String {
    logger.info("finding move")

    val moveRef = form.reference.uppercase().trim()
    if (!moveRef.matches("[A-Za-z0-9]+".toRegex())) {
      redirectAttributes.addAttribute("noResultFor", "invalid-reference")
      return "redirect:$FIND_MOVE_URL"
    }

    val maybeMove = moveService.findMoveByReferenceAndSupplier(moveRef, supplier)

    val uri =
      if (maybeMove != null) {
        "$MOVES_URL/${maybeMove.moveId}"
      } else {
        redirectAttributes.addAttribute("noResultFor", form.reference)
        FIND_MOVE_URL
      }
    return "redirect:$uri"
  }

  companion object {
    const val CHOOSE_SUPPLIER_URL = "/choose-supplier"
    const val DASHBOARD_URL = "/dashboard"
    const val FIND_MOVE_URL = "/find-move"
    const val JOURNEYS_URL = "/journeys"
    const val MOVES_BY_TYPE_URL = "/moves-by-type"
    const val MOVES_URL = "/moves"
    const val SELECT_MONTH_URL = "/select-month"
  }
}
