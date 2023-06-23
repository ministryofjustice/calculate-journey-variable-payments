package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
internal class MoveRepositoryTest {

  @Autowired
  lateinit var moveRepository: MoveRepository

  @Autowired
  lateinit var entityManager: TestEntityManager

  @Test
  fun `save report model`() {
    val move = moveM1().copy(notes = "a".repeat(1024))
    val journeyModel =
      journeyJ1(moveId = move.moveId, events = listOf(eventE1(eventId = "E1", eventableId = journeyJ1().journeyId)))
    val moveModel = move.copy(
      events = listOf(eventE1(eventId = "E2", eventableId = move.moveId)),
      journeys = listOf(journeyModel),
    )

    val persistedReport = moveRepository.save(moveModel)

    entityManager.flush()
    entityManager.clear()

    val retrievedMove = moveRepository.findById(persistedReport.moveId).get()

    assertThat(retrievedMove).isEqualTo(moveModel)
    assertThat(retrievedMove.notes).contains("a".repeat(1024))
  }

  @Test
  fun `find exactly two ordered reconcilable moves for Serco`() {
    assertThat(moveRepository.findCompletedCandidateReconcilableMoves(Supplier.SERCO, 2022, 5)).isEmpty()

    val sercoMoveMissingDropOff = moveM1().copy(
      moveId = UUID.randomUUID().toString(),
      moveDate = LocalDate.of(2022, 5, 1),
      moveType = MoveType.STANDARD,
      supplier = Supplier.SERCO,
      pickUpDateTime = LocalDate.of(2022, 5, 2).atStartOfDay(),
      dropOffOrCancelledDateTime = null,
    )

    val sercoMoveMissingMoveType = moveM1().copy(
      moveId = UUID.randomUUID().toString(),
      moveDate = LocalDate.of(2022, 5, 1),
      moveType = null,
      pickUpDateTime = LocalDate.of(2022, 5, 1).atStartOfDay(),
      dropOffOrCancelledDateTime = LocalDate.of(2022, 5, 1).atStartOfDay(),
    )

    val sercoMoveMoveTypeAndDropOffPresent = moveM1().copy(
      moveId = UUID.randomUUID().toString(),
      moveDate = LocalDate.of(2022, 5, 1),
      moveType = MoveType.STANDARD,
      pickUpDateTime = LocalDate.of(2022, 5, 1).atStartOfDay(),
      dropOffOrCancelledDateTime = LocalDate.of(2022, 5, 1).atStartOfDay(),
    )

    val geoMoveMissingDropOff = sercoMoveMissingDropOff.copy(
      moveId = UUID.randomUUID().toString(),
      supplier = Supplier.GEOAMEY,
    )

    val geoMoveMissingMoveType = sercoMoveMissingMoveType.copy(
      moveId = UUID.randomUUID().toString(),
      supplier = Supplier.GEOAMEY,
      moveDate = LocalDate.of(2022, 5, 1),
      moveType = null,
      pickUpDateTime = LocalDate.of(2022, 5, 1).atStartOfDay(),
      dropOffOrCancelledDateTime = LocalDate.of(2022, 5, 1).atStartOfDay(),
    )

    moveRepository.saveAllAndFlush(
      listOf(
        sercoMoveMissingDropOff,
        sercoMoveMissingMoveType,
        sercoMoveMoveTypeAndDropOffPresent,
        geoMoveMissingDropOff,
        geoMoveMissingMoveType,
      ),
    )

    val results = moveRepository.findCompletedCandidateReconcilableMoves(Supplier.SERCO, 2022, 5)

    assertThat(results).containsExactly(sercoMoveMissingMoveType, sercoMoveMissingDropOff)
  }
}
