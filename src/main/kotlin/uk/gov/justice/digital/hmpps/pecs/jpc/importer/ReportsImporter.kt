package uk.gov.justice.digital.hmpps.pecs.jpc.importer

import org.slf4j.LoggerFactory
import org.springframework.boot.ExitCodeGenerator
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.time.LocalDate

/**
 * This should be considered a temporary component in that as soon as we no longer need to import spreadsheets this can be removed.
 */
@Component
class ReportsImporter(private val importService: ImportService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val success = ExitCodeGenerator { 0 }

    private val failure = ExitCodeGenerator { 1 }

    /**
     * Calling this kicks off an import and returns '0' if successful or '1' if any exception is thrown (and caught).
     */
    fun import(from: LocalDate, to: LocalDate): ExitCodeGenerator {
        return Result.runCatching {
            importService.importReports(from, to)
            return success
        }.onFailure { logger.error(it.stackTraceToString()) }.getOrDefault(failure)
    }
}
