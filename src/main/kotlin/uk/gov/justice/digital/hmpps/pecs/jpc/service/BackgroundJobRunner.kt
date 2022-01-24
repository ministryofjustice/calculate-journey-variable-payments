package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val logger = loggerFor<BackgroundJobRunner>()

@Component
class BackgroundJobRunner : JobRunner {

  private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

  override fun run(label: String, job: () -> Unit) {
    executorService.submit {
      logger.info("Running background job '$label'.")

      job()
    }
  }
}
