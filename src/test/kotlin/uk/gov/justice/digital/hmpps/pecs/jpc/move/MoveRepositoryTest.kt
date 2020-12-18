package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJpaTest
internal class MoveRepositoryTest {

    @Autowired
    lateinit var moveRepository: MoveRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `save report model`() {

        val move = moveM1().copy(notes = "adfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffs" +
                "fasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfas" +
                "adfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaf" +
                "fsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaff" +
                "sfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasa" +
                "dfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsa" +
                "fafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasa" +
                "dfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafaf" +
                "affsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadf" +
                "afafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaf" +
                "fsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafa" +
                "fsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffsfas")
        val journeyModel = journeyJ1(moveId = move.moveId, events = listOf(eventE1(eventId = "E1", eventableId = journeyJ1().journeyId)))
        val moveModel = move.copy(
            events = listOf(eventE1(eventId = "E2", eventableId = move.moveId)),
            journeys = listOf(journeyModel)
        )

        val persistedReport = moveRepository.save(moveModel)

        entityManager.flush()
        entityManager.clear()

        val retrievedMove = moveRepository.findById(persistedReport.moveId).get()

        assertThat(retrievedMove).isEqualTo(moveModel)
        assertThat(retrievedMove.notes).contains("adfafafsafafaffsfasadfafafsafafaffsfasadfafafsafafaffs")
    }
}
