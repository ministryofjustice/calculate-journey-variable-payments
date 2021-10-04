package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MaintainSupplierPricingController.PriceExceptionForm
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MaintainSupplierPricingController.PriceExceptionMonth
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MaintainSupplierPricingController.PriceForm
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MaintainSupplierPricingController.Warning
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEvent
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.PriceMetadata
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier.GEOAMEY
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier.SERCO
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.SupplierPricingService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.SupplierPricingService.PriceDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@WithMockUser(roles = ["PECS_MAINTAIN_PRICE"])
class MaintainSupplierPricingControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val mockSession = MockHttpSession(wac.servletContext)

  private val currentContractualDate = LocalDate.now()

  private val currentContractualEffectiveYear = effectiveYearForDate(currentContractualDate)

  private val previousContractualDate = currentContractualDate.minusYears(1)

  private val previousContractualEffectiveYear = effectiveYearForDate(previousContractualDate)

  @MockBean
  private lateinit var actualEffectiveYear: EffectiveYear

  @MockBean
  private lateinit var service: SupplierPricingService

  private val fromAgencyId = "AAAAAA"

  private val toAgencyId = "BBBBBB"

  @BeforeEach
  fun `set up actual effective year fixture`() {
    whenever(actualEffectiveYear.current()).thenReturn(effectiveYearForDate(currentContractualDate))
  }

  @Test
  internal fun `add price for Serco for current contractual year`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    whenever(
      service.getSiteNamesForPricing(
        SERCO,
        fromAgencyId,
        toAgencyId,
        currentContractualEffectiveYear
      )
    ).thenReturn(Pair("from", "to"))

    mockMvc.get("/add-price/$fromAgencyId-$toAgencyId") { session = mockSession }
      .andExpect { model { attribute("form", PriceForm("$fromAgencyId-$toAgencyId", "0.00", "from", "to")) } }
      .andExpect {
        model {
          attribute(
            "warnings",
            listOf(Warning("Please note the added price will be effective for all instances of this journey undertaken by SERCO in the current contractual year $currentContractualEffectiveYear to ${currentContractualEffectiveYear + 1}."))
          )
        }
      }
      // including check for the navigation
      .andExpect { model { attribute("navigation", "PRICE") } }
      .andExpect { view { name("add-price") } }
      .andExpect { status { isOk() } }

    verify(service).getSiteNamesForPricing(SERCO, fromAgencyId, toAgencyId, currentContractualEffectiveYear)
  }

  @Test
  internal fun `add price for Serco for previous contractual year without existing price in current contractual year`() {
    mockSession.addSupplierAndContractualYear(SERCO, previousContractualDate)

    whenever(
      service.getSiteNamesForPricing(
        SERCO,
        fromAgencyId,
        toAgencyId,
        previousContractualEffectiveYear
      )
    ).thenReturn(Pair("from", "to"))

    mockMvc.get("/add-price/$fromAgencyId-$toAgencyId") { session = mockSession }
      .andExpect { model { attribute("form", PriceForm("$fromAgencyId-$toAgencyId", "0.00", "from", "to")) } }
      .andExpect {
        model {
          attribute(
            "warnings",
            listOf(Warning("Making this change will only affect journeys undertaken in the contractual year $previousContractualEffectiveYear to ${previousContractualEffectiveYear + 1}. You will need to apply a bulk price adjustment to calculate the new journey price in the current contractual year."))
          )
        }
      }
      .andExpect { view { name("add-price") } }
      .andExpect { status { isOk() } }

    verify(service).getSiteNamesForPricing(SERCO, fromAgencyId, toAgencyId, previousContractualEffectiveYear)
  }

  @Test
  internal fun `add price for Serco for previous contractual year with existing price in current contractual year`() {
    mockSession.addSupplierAndContractualYear(SERCO, previousContractualDate)

    whenever(
      service.getSiteNamesForPricing(
        SERCO,
        fromAgencyId,
        toAgencyId,
        previousContractualEffectiveYear
      )
    ).thenReturn(Pair("from", "to"))

    whenever(
      service.maybePrice(
        SERCO,
        fromAgencyId,
        toAgencyId,
        actualEffectiveYear.current()
      )
    ).thenReturn(PriceDto("a", "b", Money(10)))

    mockMvc.get("/add-price/$fromAgencyId-$toAgencyId") { session = mockSession }
      .andExpect { model { attribute("form", PriceForm("$fromAgencyId-$toAgencyId", "0.00", "from", "to")) } }
      .andExpect {
        model {
          attribute(
            "warnings",
            listOf(
              Warning("Please note a price for this journey already exists in the current contractual year ${actualEffectiveYear.current()} to ${actualEffectiveYear.current() + 1}."),
              Warning("Making this change will only affect journeys undertaken in the contractual year $previousContractualEffectiveYear to ${previousContractualEffectiveYear + 1}. You will need to apply a bulk price adjustment to calculate the new journey price in the current contractual year.")
            )
          )
        }
      }
      .andExpect { view { name("add-price") } }
      .andExpect { status { isOk() } }

    verify(service).getSiteNamesForPricing(SERCO, fromAgencyId, toAgencyId, previousContractualEffectiveYear)
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  internal fun `cannot initiate add price for Serco when insufficient privileges`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    mockMvc.get("/add-price/not-allowed") { session = mockSession }.andExpect { status { isForbidden() } }
  }

  @Test
  internal fun `can add up to max price for Serco in current contractual year`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    mockMvc.post("/add-price") {
      session = mockSession
      param("moveId", "$fromAgencyId-$toAgencyId")
      param("price", "9999.99")
    }
      .andExpect { redirectedUrl("/journeys") }
      .andExpect { status { is3xxRedirection() } }

    verify(service).addPriceForSupplier(SERCO, fromAgencyId, toAgencyId, Money.valueOf(9999.99), currentContractualEffectiveYear)
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  internal fun `cannot add price for Serco when insufficient privileges`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    mockMvc.post("/add-price") {
      session = mockSession
      param("moveId", "$fromAgencyId-$toAgencyId")
      param("price", "100.24")
    }.andExpect { status { isForbidden() } }
  }

  @Test
  internal fun `cannot add price with characters for Serco`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    mockMvc.post("/add-price") {
      session = mockSession
      param("moveId", "$fromAgencyId-$toAgencyId")
      param("price", "1O.00")
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "price", "Pattern") } }
      .andExpect {
        model {
          attribute(
            "warnings",
            listOf(Warning("Please note the added price will be effective for all instances of this journey undertaken by SERCO in the current contractual year $currentContractualEffectiveYear to ${currentContractualEffectiveYear + 1}."))
          )
        }
      }
      .andExpect { view { name("add-price") } }
      .andExpect { status { isOk() } }

    verify(service, never()).addPriceForSupplier(any(), any(), any(), any(), any())
  }

  @Test
  internal fun `cannot add price less than one pence for Serco`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    mockMvc.post("/add-price") {
      session = mockSession
      param("moveId", "$fromAgencyId-$toAgencyId")
      param("price", "0.001")
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "price", "Pattern") } }
      .andExpect {
        model {
          attribute(
            "warnings",
            listOf(Warning("Please note the added price will be effective for all instances of this journey undertaken by SERCO in the current contractual year $currentContractualEffectiveYear to ${currentContractualEffectiveYear + 1}."))
          )
        }
      }
      .andExpect { view { name("add-price") } }
      .andExpect { status { isOk() } }

    verify(service, never()).addPriceForSupplier(any(), any(), any(), any(), any())
  }

  @Test
  internal fun `cannot add negative price for Serco`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    mockMvc.post("/add-price") {
      session = mockSession
      param("moveId", "$fromAgencyId-$toAgencyId")
      param("price", "-10.00")
    }
      .andExpect { model { attributeHasFieldErrorCode("form", "price", "Pattern") } }
      .andExpect {
        model {
          attribute(
            "warnings",
            listOf(Warning("Please note the added price will be effective for all instances of this journey undertaken by SERCO in the current contractual year $currentContractualEffectiveYear to ${currentContractualEffectiveYear + 1}."))
          )
        }
      }
      .andExpect { view { name("add-price") } }
      .andExpect { status { isOk() } }

    verify(service, never()).addPriceForSupplier(any(), any(), any(), any(), any())
  }

  @Test
  internal fun `can initiate update price for Geoamey including price history and exceptions in current contractual year`() {
    mockSession.addSupplierAndContractualYear(GEOAMEY, currentContractualDate)

    whenever(
      service.maybePrice(
        GEOAMEY,
        fromAgencyId,
        toAgencyId,
        currentContractualEffectiveYear
      )
    ).thenReturn(PriceDto("from", "to", Money(1000)).apply { exceptions[7] = Money(100) })

    val priceHistoryDateTime = LocalDateTime.now()
    val priceHistory = PriceMetadata(
      GEOAMEY,
      fromAgencyId,
      toAgencyId,
      currentContractualEffectiveYear,
      Money.valueOf(10.00).pounds()
    )
    val priceEvent = AuditEvent(AuditEventType.JOURNEY_PRICE, priceHistoryDateTime, "_TERMINAL_", priceHistory)

    whenever(service.priceHistoryForJourney(GEOAMEY, fromAgencyId, toAgencyId)).thenReturn(setOf(priceEvent))

    mockMvc.get("/update-price/$fromAgencyId-$toAgencyId") { session = mockSession }
      .andExpect { model { attribute("form", PriceForm("$fromAgencyId-$toAgencyId", "10.00", "from", "to")) } }
      .andExpect {
        model {
          attribute(
            "warnings",
            listOf(Warning("Please note the added price will be effective for all instances of this journey undertaken by GEOAMEY in the current contractual year $currentContractualEffectiveYear to ${currentContractualEffectiveYear + 1}."))
          )
        }
      }
      .andExpect {
        model {
          attribute(
            "history",
            listOf(
              PriceHistoryDto(
                priceHistoryDateTime,
                "Journey priced at £10.00. Effective from $currentContractualEffectiveYear to ${currentContractualEffectiveYear + 1}.",
                "SYSTEM"
              )
            )
          )
        }
      }
      .andExpect { model { attribute("existingExceptions", listOf(PriceExceptionMonth(Month.JULY, true, Money(100)))) } }
      .andExpect { model { attribute("exceptionsForm", PriceExceptionForm("$fromAgencyId-$toAgencyId", mapOf(7 to Money(100)), exceptionPrice = "0.00")) } }
      .andExpect { view { name("update-price") } }
      .andExpect { status { isOk() } }

    verify(service).maybePrice(GEOAMEY, fromAgencyId, toAgencyId, currentContractualEffectiveYear)
  }

  @Test
  internal fun `can initiate update price for Geoamey including price history for the previous contractual year`() {
    mockSession.addSupplierAndContractualYear(GEOAMEY, previousContractualDate)

    whenever(
      service.maybePrice(
        GEOAMEY,
        fromAgencyId,
        toAgencyId,
        previousContractualEffectiveYear
      )
    ).thenReturn(PriceDto("from", "to", Money.valueOf(10.00)))

    val priceHistory = PriceMetadata(
      GEOAMEY,
      fromAgencyId,
      toAgencyId,
      previousContractualEffectiveYear,
      Money.valueOf(10.00).pounds()
    )

    val priceHistoryDateTime = LocalDateTime.now()
    val priceEvent = AuditEvent(AuditEventType.JOURNEY_PRICE, priceHistoryDateTime, "_TERMINAL_", priceHistory)

    whenever(service.priceHistoryForJourney(GEOAMEY, fromAgencyId, toAgencyId)).thenReturn(setOf(priceEvent))

    mockMvc.get("/update-price/$fromAgencyId-$toAgencyId") { session = mockSession }
      .andExpect { model { attribute("form", PriceForm("$fromAgencyId-$toAgencyId", "10.00", "from", "to")) } }
      .andExpect {
        model {
          attribute(
            "warnings",
            listOf(
              Warning("Making this change will only affect journeys undertaken in the contractual year $previousContractualEffectiveYear to ${previousContractualEffectiveYear + 1}. You will need to apply a bulk price adjustment to calculate the new journey price in the current contractual year.")
            )
          )
        }
      }
      .andExpect {
        model {
          attribute(
            "history",
            listOf(
              PriceHistoryDto(
                priceHistoryDateTime,
                "Journey priced at £10.00. Effective from $previousContractualEffectiveYear to ${previousContractualEffectiveYear + 1}.",
                "SYSTEM"
              )
            )
          )
        }
      }
      .andExpect { view { name("update-price") } }
      .andExpect { status { isOk() } }

    verify(service).maybePrice(GEOAMEY, fromAgencyId, toAgencyId, previousContractualEffectiveYear)
  }

  @Test
  internal fun `can initiate update price for Geoamey including price history for the previous contractual year with existing price in current contractual year`() {
    mockSession.addSupplierAndContractualYear(GEOAMEY, previousContractualDate)

    whenever(
      service.maybePrice(
        GEOAMEY,
        fromAgencyId,
        toAgencyId,
        previousContractualEffectiveYear
      )
    ).thenReturn(PriceDto("from", "to", Money(1000)))

    whenever(
      service.maybePrice(
        GEOAMEY,
        fromAgencyId,
        toAgencyId,
        currentContractualEffectiveYear
      )
    ).thenReturn(PriceDto("from", "to", Money(1500)))

    val priceHistoryDateTime = LocalDateTime.now()
    val priceHistory = PriceMetadata(
      GEOAMEY,
      fromAgencyId,
      toAgencyId,
      previousContractualEffectiveYear,
      Money.valueOf(10.00).pounds()
    )
    val priceEvent = AuditEvent(AuditEventType.JOURNEY_PRICE, priceHistoryDateTime, "_TERMINAL_", priceHistory)

    whenever(service.priceHistoryForJourney(GEOAMEY, fromAgencyId, toAgencyId)).thenReturn(setOf(priceEvent))

    mockMvc.get("/update-price/$fromAgencyId-$toAgencyId") { session = mockSession }
      .andExpect { model { attribute("form", PriceForm("$fromAgencyId-$toAgencyId", "10.00", "from", "to")) } }
      .andExpect {
        model {
          attribute(
            "warnings",
            listOf(
              Warning("Please note a price for this journey already exists in the current contractual year ${actualEffectiveYear.current()} to ${actualEffectiveYear.current() + 1}."),
              Warning("Making this change will only affect journeys undertaken in the contractual year $previousContractualEffectiveYear to ${previousContractualEffectiveYear + 1}. You will need to apply a bulk price adjustment to calculate the new journey price in the current contractual year.")
            )
          )
        }
      }
      .andExpect {
        model {
          attribute(
            "history",
            listOf(
              PriceHistoryDto(
                priceHistoryDateTime,
                "Journey priced at £10.00. Effective from $previousContractualEffectiveYear to ${previousContractualEffectiveYear + 1}.",
                "SYSTEM"
              )
            )
          )
        }
      }
      .andExpect { view { name("update-price") } }
      .andExpect { status { isOk() } }

    verify(service).maybePrice(GEOAMEY, fromAgencyId, toAgencyId, previousContractualEffectiveYear)
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  internal fun `cannot initiate update price for Geoamey when insufficient privileges`() {
    mockSession.addSupplierAndContractualYear(GEOAMEY, currentContractualDate)

    mockMvc.get("/update-price/$fromAgencyId-$toAgencyId") { session = mockSession }
      .andExpect { status { isForbidden() } }
  }

  @Test
  internal fun `can add a price exception`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    whenever(
      service.maybePrice(
        SERCO,
        fromAgencyId,
        toAgencyId,
        currentContractualEffectiveYear
      )
    ).thenReturn(PriceDto("a", "b", Money(10)))

    mockMvc.post("/add-price-exception") {
      session = mockSession
      param("moveId", "$fromAgencyId-$toAgencyId")
      param("exceptionPrice", "50.00")
      param("exceptionMonth", "SEPTEMBER")
    }
      .andExpect { flash { attribute("flashMessage", "price-exception-created") } }
      .andExpect { flash { attribute("flashAttrExceptionPrice", "50.00") } }
      .andExpect { flash { attribute("flashAttrExceptionMonth", "SEPTEMBER") } }
      .andExpect { flash { attribute("flashAttrLocationFrom", "a") } }
      .andExpect { flash { attribute("flashAttrLocationTo", "b") } }
      .andExpect { redirectedUrl("/update-price/AAAAAA-BBBBBB#price-exceptions") }
      .andExpect { status { is3xxRedirection() } }

    verify(service).addPriceException(SERCO, fromAgencyId, toAgencyId, currentContractualEffectiveYear, Month.SEPTEMBER, Money.valueOf(50.00))
  }

  @Test
  internal fun `cannot add an invalid price exception`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    whenever(
      service.maybePrice(
        SERCO,
        fromAgencyId,
        toAgencyId,
        currentContractualEffectiveYear
      )
    ).thenReturn(PriceDto("a", "b", Money(10)))

    mockMvc.post("/add-price-exception") {
      session = mockSession
      param("moveId", "$fromAgencyId-$toAgencyId")
      param("exceptionPrice", "5A.00")
      param("exceptionMonth", "SEPTEMBER")
    }
      .andExpect { redirectedUrl("/update-price/$fromAgencyId-$toAgencyId#price-exceptions") }
      .andExpect { flash { attribute("flashError", "add-price-exception-error") } }
      .andExpect { status { is3xxRedirection() } }

    verify(service, never()).addPriceException(any(), any(), any(), any(), any(), any())
  }

  @Test
  internal fun `cannot add an exception for the same price`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    whenever(
      service.maybePrice(
        SERCO,
        fromAgencyId,
        toAgencyId,
        currentContractualEffectiveYear
      )
    ).thenReturn(PriceDto("a", "b", Money(100)))

    mockMvc.post("/add-price-exception") {
      session = mockSession
      param("moveId", "$fromAgencyId-$toAgencyId")
      param("exceptionPrice", "1.00")
      param("exceptionMonth", "SEPTEMBER")
    }
      .andExpect { redirectedUrl("/update-price/$fromAgencyId-$toAgencyId#price-exceptions") }
      .andExpect { flash { attribute("flashError", "add-price-exception-error") } }
      .andExpect { status { is3xxRedirection() } }

    verify(service, never()).addPriceException(any(), any(), any(), any(), any(), any())
  }

  @Test
  internal fun `can remove an existing price exception`() {
    mockSession.addSupplierAndContractualYear(SERCO, currentContractualDate)

    whenever(
      service.removePriceException(
        SERCO,
        fromAgencyId,
        toAgencyId,
        currentContractualEffectiveYear,
        Month.SEPTEMBER
      )
    ).thenReturn(PriceDto("a", "b", Money(100)))

    mockMvc.post("/remove-price-exception") {
      session = mockSession
      param("moveId", "$fromAgencyId-$toAgencyId")
      param("month", "SEPTEMBER")
    }
      .andExpect { flash { attribute("flashMessage", "price-exception-removed") } }
      .andExpect { flash { attribute("flashAttrExceptionMonth", "SEPTEMBER") } }
      .andExpect { flash { attribute("flashAttrLocationFrom", "a") } }
      .andExpect { flash { attribute("flashAttrLocationTo", "b") } }
      .andExpect { redirectedUrl("/update-price/AAAAAA-BBBBBB#price-exceptions") }
      .andExpect { status { is3xxRedirection() } }

    verify(service).removePriceException(SERCO, fromAgencyId, toAgencyId, currentContractualEffectiveYear, Month.SEPTEMBER)
  }

  private fun MockHttpSession.addSupplierAndContractualYear(supplier: Supplier, contractualYearDate: LocalDate) {
    this.setAttribute("supplier", supplier)
    this.setAttribute("date", contractualYearDate)
  }
}
