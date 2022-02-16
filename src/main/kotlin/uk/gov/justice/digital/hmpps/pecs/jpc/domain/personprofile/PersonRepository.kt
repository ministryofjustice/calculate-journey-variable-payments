package uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile

import org.springframework.data.jpa.repository.JpaRepository

interface PersonRepository : JpaRepository<Person, String>
