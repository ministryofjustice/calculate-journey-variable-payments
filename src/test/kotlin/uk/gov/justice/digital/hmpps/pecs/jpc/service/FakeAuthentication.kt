package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

internal class FakeAuthentication : BeforeEachCallback, AfterEachCallback {

  private val authentication: Authentication = mock { on { name } doReturn "mock username" }
  private val securityContext: SecurityContext = mock { on { authentication } doReturn authentication }

  override fun beforeEach(context: ExtensionContext?) {
    SecurityContextHolder.setContext(securityContext)
  }

  override fun afterEach(context: ExtensionContext?) {
    SecurityContextHolder.clearContext()
  }
}
