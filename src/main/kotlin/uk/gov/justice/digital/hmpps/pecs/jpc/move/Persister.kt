package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.springframework.data.jpa.repository.JpaRepository

fun <T> saveFlushAndClear(repo: JpaRepository<T, String>, entities: MutableCollection<T>) {
  repo.saveAll(entities)
  repo.flush()
  entities.clear()
}
