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
        val eventToSave = Event(
                id= UUID.randomUUID(),
                type="MoveCancel",
                actionedBy="serco",
                eventableType="move",
                eventableId= UUID.randomUUID(),
                details= null,
                occurredAt=occurredAndRecordedAt,
                recordedAt=occurredAndRecordedAt,
                notes="")
        val event = repository.save(eventToSave)

        entityManager.flush()

        assertThat(repository.findById(eventToSave.id).orElseThrow()).isEqualTo(eventToSave)
    }

    @Test
    fun `should throw constraint violation if type, actionedBy or eventableType is empty`() {
        val eventToSave = Event(
                id= UUID.randomUUID(),
                type="",
                actionedBy="",
                eventableType="",
                eventableId= UUID.randomUUID(),
                details= null,
                occurredAt=occurredAndRecordedAt,
                recordedAt=occurredAndRecordedAt,
                notes="")
        assertThatThrownBy {
            repository.save(eventToSave)
            entityManager.flush()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }
}
