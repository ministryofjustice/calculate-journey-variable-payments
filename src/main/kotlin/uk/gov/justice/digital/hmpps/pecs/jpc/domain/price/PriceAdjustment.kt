package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * The purpose of this entity is to identify if there is supplier price adjustment (already) in progress. Its lack of
 * existence indicates no adjustment is in progress.
 *
 * To avoid pricing adjustment issues in a distributed environment there can only ever be one adjustment in progress for
 * a supplier at any given point in time.
 */
@Entity
@Table(
  name = "PRICE_ADJUSTMENTS",
  indexes = [
    Index(
      name = "SUPPLIER_UNIQUE",
      columnList = "supplier",
      unique = true
    )
  ]
)
data class PriceAdjustment(
  @Id
  @Column(name = "id", nullable = false)
  val id: UUID = UUID.randomUUID(),

  @Column(name = "supplier", nullable = false)
  @Enumerated(EnumType.STRING)
  val supplier: Supplier,

  @Column(name = "added_at", nullable = false)
  val addedAt: LocalDateTime = LocalDateTime.now(),

  @Column(name = "effective_year", nullable = false)
  val effectiveYear: Int,

  @Column(name = "multiplier", nullable = false)
  val multiplier: BigDecimal
)
