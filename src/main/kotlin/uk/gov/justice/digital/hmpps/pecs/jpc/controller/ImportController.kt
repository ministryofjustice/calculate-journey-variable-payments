package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.pecs.jpc.location.importer.LocationsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.importer.PriceImporter
import java.util.concurrent.atomic.AtomicBoolean

@RestController
class ImportController(private val locationsImporter: LocationsImporter, private val priceImporter: PriceImporter) {

    // Imposed a crude temporary locking solution to prevent DB clashes/conflicts.
    private val lock = AtomicBoolean(false)

    @PostMapping("/locations/import")
    fun locations(): String {
        if (lock.compareAndSet(false, true)) {
            try {
                return locationsImporter.import().name
            } finally {
                lock.set(false)
            }
        }

        return "An import is currently in progress...";
    }

    @PostMapping("/prices/import")
    fun prices(): String {
        if (lock.compareAndSet(false, true)) {
            try {
                return priceImporter.import().name
            } finally {
                lock.set(false)
            }
        }

        return "An import is currently in progress...";
    }
}
