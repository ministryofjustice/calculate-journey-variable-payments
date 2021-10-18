package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.slf4j.LoggerFactory
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
import uk.gov.justice.digital.hmpps.pecs.jpc.service.JourneyService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.MonthYearParser
import java.time.LocalDate
import javax.validation.Valid

data class MonthsWidget(val currentMonth: LocalDate, val nextMonth: LocalDate, val previousMonth: LocalDate)

@Controller
@SessionAttributes(
  SUPPLIER_ATTRIBUTE,
  DATE_ATTRIBUTE,
  START_OF_MONTH_DATE_ATTRIBUTE,
  END_OF_MONTH_DATE_ATTRIBUTE
)
class HtmlController(
  @Autowired val moveService: MoveService,
  @Autowired val journeyService: JourneyService,
  @Autowired val timeSource: TimeSource,
  @Autowired val actualEffectiveYear: EffectiveYear
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @ModelAttribute("navigation")
  fun navigation() = "SUMMARY"

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
    @ModelAttribute(name = DATE_ATTRIBUTE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startOfMonth: LocalDate,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    model: ModelMap
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
          previousMonth = (startOfMonth.minusMonths(1))
        )
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
    model: ModelMap
  ): ModelAndView {
    logger.info("$supplier move $moveId")

    val maybeMove = moveService.moveWithPersonJourneysAndEvents(moveId, supplier, model.getStartOfMonth().month)

    return maybeMove?.let {
      model.addAttribute(MOVE_ATTRIBUTE, maybeMove)
      ModelAndView("move")
    } ?: ModelAndView("error/404", HttpStatus.NOT_FOUND)
  }

  @RequestMapping(JOURNEYS_URL)
  fun journeys(
    @ModelAttribute(name = DATE_ATTRIBUTE) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startOfMonth: LocalDate,
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
      required = false
    ) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) requestParamStartOfMonth: LocalDate?,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    model: ModelMap
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
          previousMonth = (startOfMonth.minusMonths(1))
        )
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
  fun jumpToMonth(@Valid @ModelAttribute("form") form: JumpToMonthForm, result: BindingResult, model: ModelMap): Any {
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
    @Valid @ModelAttribute("form") form: FindMoveForm,
    @ModelAttribute(name = SUPPLIER_ATTRIBUTE) supplier: Supplier,
    result: BindingResult,
    model: ModelMap,
    redirectAttributes: RedirectAttributes
  ): String {
    logger.info("finding move")

    val moveRef = form.reference.uppercase().trim()
    if (!moveRef.matches("[A-Za-z0-9]+".toRegex())) return "redirect:$FIND_MOVE_URL/?no-results-for=invalid-reference"

    val maybeMove = moveService.findMoveByReferenceAndSupplier(moveRef, supplier)
    val uri =
      maybeMove.orElse(null)?.let { "$MOVES_URL/${it.moveId}" } ?: "$FIND_MOVE_URL/?no-results-for=${form.reference}"
    return "redirect:$uri"
  }

  companion object {
    const val DASHBOARD_URL = "/dashboard"
    const val SELECT_MONTH_URL = "/select-month"
    const val MOVES_BY_TYPE_URL = "/moves-by-type"
    const val MOVES_URL = "/moves"
    const val JOURNEYS_URL = "/journeys"
    const val FIND_MOVE_URL = "/find-move"
    const val CHOOSE_SUPPLIER_URL = "/choose-supplier"

    fun routes(): Array<String> =
      arrayOf(DASHBOARD_URL, JOURNEYS_URL, MOVES_BY_TYPE_URL, SELECT_MONTH_URL)
  }
}
