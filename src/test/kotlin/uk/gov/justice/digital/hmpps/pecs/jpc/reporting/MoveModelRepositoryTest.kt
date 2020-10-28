package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("test")
@DataJpaTest
internal class MoveModelRepositoryTest {

    @Autowired
    lateinit var moveModelRepository: MoveModelRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `save report model`() {

        val moveModel = moveModel(moveId = UUID.randomUUID().toString())
        val persistedReport = moveModelRepository.save(moveModel)

        entityManager.flush()
        entityManager.clear()

        val retrievedReport = moveModelRepository.findById(persistedReport.moveId).get()

        assertThat(retrievedReport).isEqualTo(moveModel)
    }
}
