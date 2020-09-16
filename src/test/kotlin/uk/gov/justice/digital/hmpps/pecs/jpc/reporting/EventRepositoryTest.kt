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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.validation.ConstraintViolationException

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@DataJpaTest
class EventRepositoryTest {

    val occurredAndRecordedAt = LocalDateTime.parse("2020-06-16T10:20:30+01:00", DateTimeFormatter.ISO_DATE_TIME)

    @Autowired
    lateinit var repository: EventRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `can save Event`() {
        val eventToSave = cannedMoveEvent()
        repository.save(eventToSave)

        entityManager.flush()

        // details not saved in db so should be null
        assertThat(repository.findById(eventToSave.id).orElseThrow()).isEqualTo(eventToSave.copy(details = null))
    }

    @Test
    fun `can retrieve Event using list of eventableIds`() {
        val eventWithCorrectEventableId = cannedJourneyEvent()
        val event2WithCorrectEventableId = eventWithCorrectEventableId.copy(id = UUID.randomUUID())
        val event3WithIncorrectEventableId = eventWithCorrectEventableId.copy(id = UUID.randomUUID(), eventableId = UUID.randomUUID())

        repository.save(eventWithCorrectEventableId)
        repository.save(event2WithCorrectEventableId)
        repository.save(event3WithIncorrectEventableId)

        entityManager.flush()

        val savedEvents = repository.findByEventableIds(listOf(eventWithCorrectEventableId.eventableId))

        // Only 2 events should come back
        assertThat(savedEvents.map { it.id }.toSet()).isEqualTo(setOf(eventWithCorrectEventableId.id, event2WithCorrectEventableId.id))
    }

    @Test
    fun `should throw constraint violation if type is empty`() {
        val eventToSave = cannedMoveEvent().copy(type = "")
        assertThatThrownBy {
            repository.save(eventToSave)
            entityManager.flush()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }
}
