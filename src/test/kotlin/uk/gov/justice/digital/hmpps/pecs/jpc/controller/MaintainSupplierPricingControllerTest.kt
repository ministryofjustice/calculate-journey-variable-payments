package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.service.SupplierPricingService

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class MaintainSupplierPricingControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  private val mockSession = MockHttpSession(wac.servletContext)

  @MockBean
  lateinit var service: SupplierPricingService

  private val fromAgencyId = "AAAAAA"

  private val toAgencyId = "BBBBBB"

  @Test
  internal fun `can initiate add price for Serco`() {
    mockSession.apply {
      this.setAttribute("supplier", Supplier.SERCO)
    }

    whenever(service.getSiteNamesForPricing(Supplier.SERCO, fromAgencyId, toAgencyId)).thenReturn(Pair("from", "to"))

    mockMvc.get("/add-price/${fromAgencyId}-${toAgencyId}") { session = mockSession}
            .andExpect { model { attribute("form", MaintainSupplierPricingController.PriceForm("${fromAgencyId}-${toAgencyId}", "0.00", "from", "to")) } }
            .andExpect { view { name("add-price") } }
            .andExpect { status { isOk } }

    verify(service).getSiteNamesForPricing(Supplier.SERCO, fromAgencyId, toAgencyId)
  }

  @Test
  internal fun `can add price for Serco`() {
    mockSession.apply {
      this.setAttribute("supplier", Supplier.SERCO)
    }

    mockMvc.post("/add-price") {
      session = mockSession
      param("moveId", "${fromAgencyId}-${toAgencyId}")
      param("price", "100.24")
    }
            .andExpect { redirectedUrl("/journeys") }
            .andExpect { status { is3xxRedirection } }

    verify(service).addPriceForSupplier(Supplier.SERCO, fromAgencyId, toAgencyId, Money.valueOf(100.24))
  }

  @Test
  internal fun `cannot add price with characters for Serco`() {
    mockSession.apply {
      this.setAttribute("supplier", Supplier.SERCO)
    }

    mockMvc.post("/add-price") {
      session = mockSession
      param("moveId", "${fromAgencyId}-${toAgencyId}")
      param("price", "1O.00")
    }
            .andExpect { model { attributeHasFieldErrorCode("form", "price", "Invalid price") } }
            .andExpect { view { name("add-price") } }
            .andExpect { status { isOk } }

    verify(service, never()).addPriceForSupplier(any(), any(), any(), any())
  }

  @Test
  internal fun `cannot add negative price for Serco`() {
    mockSession.apply {
      this.setAttribute("supplier", Supplier.SERCO)
    }

    mockMvc.post("/add-price") {
      session = mockSession
      param("moveId", "${fromAgencyId}-${toAgencyId}")
      param("price", "-10.00")
    }
            .andExpect { model { attributeHasFieldErrorCode("form", "price", "Invalid price") } }
            .andExpect { view { name("add-price") } }
            .andExpect { status { isOk } }

    verify(service, never()).addPriceForSupplier(any(), any(), any(), any())
  }
}
