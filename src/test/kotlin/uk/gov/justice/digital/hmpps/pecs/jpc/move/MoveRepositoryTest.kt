package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("test")
@DataJpaTest
internal class MoveRepositoryTest {

    @Autowired
    lateinit var moveRepository: MoveRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `save report model`() {

        val moveModel = move(moveId = UUID.randomUUID().toString())
        val persistedReport = moveRepository.save(moveModel)

        entityManager.flush()
        entityManager.clear()

        val retrievedReport = moveRepository.findById(persistedReport.moveId).get()

        assertThat(retrievedReport).isEqualTo(moveModel)
    }
}