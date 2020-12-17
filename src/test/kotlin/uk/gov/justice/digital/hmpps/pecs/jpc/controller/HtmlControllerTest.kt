package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.move.move
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveService
import java.time.LocalDate
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class HtmlControllerTest(@Autowired private val wac: WebApplicationContext) {

  private val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()

  @MockBean
  lateinit var moveService: MoveService

  @Test
  internal fun `find a move by valid lowercase reference id with whitespace correctly redirects to move details page`() {

    whenever(moveService.findMoveByReference("REF1")).thenReturn(Optional.of(move()))

    mockMvc.post("/find-move") {
      param("reference", "ref1 ")
    }
            .andExpect { redirectedUrl("/moves/M1") }
            .andExpect { status { is3xxRedirection } }

    verify(moveService).findMoveByReference("REF1")
  }

  @Test
  internal fun `find a move by a non-existent reference id calls the move service then redirects to search form`() {

    whenever(moveService.findMoveByReference("REF1")).thenReturn(Optional.of(move()))

    mockMvc.post("/find-move") {
      param("reference", "nonexistentref")
    }
            .andExpect { redirectedUrl("/find-move/?no-results-for=nonexistentref") }
            .andExpect { status { is3xxRedirection } }

    verify(moveService).findMoveByReference("NONEXISTENTREF")
  }

  @Test
  internal fun `find a move by invalid reference id redirects to search form without calling the move service`() {

    whenever(moveService.findMoveByReference("REF1")).thenReturn(Optional.of(move()))

    mockMvc.post("/find-move") {
      param("reference", "select * from moves")
    }
            .andExpect { redirectedUrl("/find-move/?no-results-for=invalid-reference") }
            .andExpect { status { is3xxRedirection } }

    verifyNoMoreInteractions(moveService)
  }
}
