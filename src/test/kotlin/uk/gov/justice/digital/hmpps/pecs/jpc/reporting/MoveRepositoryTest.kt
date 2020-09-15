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
class MoveRepositoryTest {

    @Autowired
    lateinit var repository: MoveRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `can save Event`() {
        val cannedMove = cannedMove()
        val move = repository.save(cannedMove)

        entityManager.flush()

        assertThat(repository.findById(cannedMove.id).orElseThrow()).isEqualTo(cannedMove)
    }

    @Test
    fun `should throw constraint violation if status is empty`() {
        val moveToSave = cannedMove().copy(status = "")
        assertThatThrownBy {
            repository.save(moveToSave)
            entityManager.flush()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }
}
