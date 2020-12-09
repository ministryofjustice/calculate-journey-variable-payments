package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import java.time.Clock
import java.time.LocalDateTime

@Configuration
class ImporterConfiguration {

  @Autowired
  private lateinit var resourceLoader: ResourceLoader

  @Autowired
  private lateinit var priceRepository: PriceRepository

  @Bean
  fun jpcTemplateProvider(@Value("\${export-files.template}") templateFileLocation: String): JPCTemplateProvider {
    return JPCTemplateProvider { resourceLoader.getResource(templateFileLocation).inputStream }
  }

  @Bean
  fun timeSource() = TimeSource { LocalDateTime.now(Clock.systemDefaultZone()) }

  @Bean
  fun supplierPrices() = SupplierPrices { supplier -> priceRepository.findBySupplier(supplier) }
}
