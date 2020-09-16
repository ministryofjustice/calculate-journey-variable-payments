package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.validation.ConstraintViolationException

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@DataJpaTest
class MoveRepositoryTest {

    @Autowired
    lateinit var moveRepository: MoveRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var journeyRepository: JourneyRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `can save Move and Events, and retrieve Move Events`() {
        val cannedMove = cannedMove()
        moveRepository.save(cannedMove)

        val cannedMoveEvent = cannedMoveEvent()
        eventRepository.save(cannedMoveEvent)

        entityManager.flush()
        entityManager.clear()

        val retrievedMove = moveRepository.findById(cannedMove.id)
        assertThat(retrievedMove.get().events.toList()[0].id).isEqualTo(cannedMoveEvent.id)
    }

    @Test
    fun `can save Move, Journeys and retrieve Journey from Move`() {
        val cannedMove = cannedMove()
        moveRepository.save(cannedMove)

        val cannedJourney = cannedJourney()
        journeyRepository.save(cannedJourney)

        val cannedJourneyEvent = cannedJourneyEvent()
        eventRepository.save(cannedJourneyEvent)

        entityManager.flush()
        entityManager.clear()

        val retrievedMove = moveRepository.findById(cannedMove.id)
        val firstRetrievedJourney = retrievedMove.get().journeys.toList()[0]
        assertThat(firstRetrievedJourney.id).isEqualTo(cannedJourney.id)
    }

    @Test
    fun `should throw constraint violation if status is empty`() {
        val moveToSave = cannedMove().copy(status = "")
        assertThatThrownBy {
            moveRepository.save(moveToSave)
            entityManager.flush()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }
}
