package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Component
class BackgroundJobRunner : JobRunner {

  private val logger = LoggerFactory.getLogger(javaClass)

  private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

  override fun run(label: String, job: () -> Unit) {
    executorService.submit {
      logger.info("Running background job '$label'.")

      job()
    }
  }
}
