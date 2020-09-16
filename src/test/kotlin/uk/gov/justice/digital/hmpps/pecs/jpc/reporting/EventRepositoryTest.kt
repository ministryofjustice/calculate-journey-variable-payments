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
        val eventToSave = cannedEvent()
        repository.save(eventToSave)

        entityManager.flush()

        // details not saved in db so should be null
        assertThat(repository.findById(eventToSave.id).orElseThrow()).isEqualTo(eventToSave.copy(details = null))
    }

//    @Test
//    fun `can retrieve Event by eventType and eventId`() {
//        val eventToSave = cannedEvent()
//        repository.save(eventToSave)
//
//        entityManager.flush()
//
//        val retrievedEntity = repository.findByEventableTypeAndEventableIds("move", listOf(UUID.fromString("02b4c0f5-4d85-4fb6-be6c-53d74b85bf2e")))
//        assertThat(repository.findById(eventToSave.id).orElseThrow()).isEqualTo(eventToSave.copy(details = null))
//    }

    @Test
    fun `should throw constraint violation if type is empty`() {
        val eventToSave = cannedEvent().copy(type = "")
        assertThatThrownBy {
            repository.save(eventToSave)
            entityManager.flush()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }
}
