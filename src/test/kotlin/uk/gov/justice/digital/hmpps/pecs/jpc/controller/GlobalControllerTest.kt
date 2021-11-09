package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@TestPropertySource(
  properties = [
    "HMPPS_AUTH_BASE_URI=http://fake_auth_base_url",
    "FEEDBACK_URL=http://fake_feedback_url"
  ]
)
class GlobalControllerTest(@Autowired val controller: GlobalController) {

  @Test
  fun `retrieval of global attributes`() {
    assertThat(controller.hmppsUrl()).isEqualTo("http://fake_auth_base_url")
    assertThat(controller.manageYourDetailsUrl()).isEqualTo("http://fake_auth_base_url/account-details")
    assertThat(controller.feedbackUrl()).isEqualTo("http://fake_feedback_url")
  }
}
