package uk.gov.justice.digital.hmpps.pecs.jpc.price

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table

/**
 * The purpose of this entity is to identify if there is an uplift already in progress.  Its lack of existence indicates
 * no uplift is in progress.
 *
 * To avoid pricing uplift issues in a distributed environment there can only ever be be one uplift in progress for a
 * supplier at any given point in time.
 */
@Entity
@Table(
  name = "SUPPLIER_PRICE_UPLIFTS",
  indexes = [
    Index(
      name = "SUPPLIER_UNIQUE",
      columnList = "supplier",
      unique = true
    )
  ]
)
data class SupplierPriceUplift(
  @Id
  @Column(name = "id", nullable = false)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "supplier", nullable = false)
  @Enumerated(EnumType.STRING)
  val supplier: Supplier,

  @Column(name = "multiplier", nullable = false)
  val multiplier: Double,

  @Column(name = "effective_year", nullable = false)
  val effectiveYear: Int,

  @Column(name = "added_at", nullable = false)
  val addedAt: LocalDateTime = LocalDateTime.now()
)
