package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.location.importer.LocationsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer.PriceImporter
import java.util.concurrent.atomic.AtomicBoolean

@Service
class ImportService(private val locationsImporter: LocationsImporter, private val priceImporter: PriceImporter) {

    // Imposing a temporary crude locking solution to prevent DB clashes/conflicts.  I know this will not scale!
    private val lock = AtomicBoolean(false)

    fun locations(): String {
        if (lock.compareAndSet(false, true)) {
            try {
                return locationsImporter.import().name
            } finally {
                lock.set(false)
            }
        }

        return ImportStatus.IN_PROGRESS.name
    }

    fun prices(): String {
        if (lock.compareAndSet(false, true)) {
            try {
                return priceImporter.import().name
            } finally {
                lock.set(false)
            }
        }

        return ImportStatus.IN_PROGRESS.name
    }
}
