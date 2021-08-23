package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * This is used by the [ImportCommands] component for parsing [String] to [LocalDate].
 */
@Component
class LocalDateConverter : Converter<String, LocalDate> {
  override fun convert(source: String): LocalDate? = LocalDate.parse(source)
}
