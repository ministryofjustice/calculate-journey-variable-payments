package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Profile
import java.io.StringReader
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class ReportReaderParserTest(@Autowired val reportReaderParser: ReportReaderParser) {

  @Test
  fun `read and parse multiple profiles with simple string reader`() {
    val profiles = """
    {"id":"PR1","updated_at": "2022-02-09T00:00:00+00:00", "person_id":"PE1"}
    {"id":"PR2","updated_at": "2022-02-09T00:00:00+00:00", "person_id":"PE2"}
    """.trimIndent()

    val parsedProfiles = mutableListOf<Profile>()

    ReportReaderParser { StringReader(profiles) }.forEach(
      "_",
      { Profile.fromJson(it) }
    ) {
      parsedProfiles += it
    }

    assertThat(parsedProfiles).containsExactlyInAnyOrder(
      Profile(
        profileId = "PR1",
        updatedAt = LocalDate.of(2022, 2, 9).atStartOfDay(),
        personId = "PE1"
      ),
      Profile(
        profileId = "PR2",
        updatedAt = LocalDate.of(2022, 2, 9).atStartOfDay(),
        personId = "PE2"
      ),
    )
  }

  @Test
  fun `read and parse multiple profiles from reporting file`() {
    val parsedProfiles = mutableListOf<Profile>()

    reportReaderParser.forEach(
      "2022/01/02/2022-01-02-profiles.jsonl",
      { Profile.fromJson(it) }
    ) {
      parsedProfiles += it
    }

    assertThat(parsedProfiles).containsExactlyInAnyOrder(
      Profile(
        profileId = "PR1",
        updatedAt = LocalDateTime.of(2022, 1, 2, 0, 0, 0),
        personId = "PE1"
      ),
      Profile(
        profileId = "PR2",
        updatedAt = LocalDateTime.of(2022, 1, 2, 0, 0, 0),
        personId = "PE2"
      ),
      Profile(
        profileId = "PR3",
        updatedAt = LocalDateTime.of(2022, 1, 2, 0, 0, 0),
        personId = "PE3"
      ),
    )
  }
}
