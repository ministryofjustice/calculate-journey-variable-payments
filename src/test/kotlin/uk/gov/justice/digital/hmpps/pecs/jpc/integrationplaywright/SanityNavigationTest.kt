package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SanityNavigationTest : PlayWrightTest() {

  @Test
  fun sanityNavigation() {
    val target = System.getenv("APP_BASE_URL")?.trim().takeUnless { it.isNullOrBlank() } ?: "http://my-app:8080"

    page!!.navigate("http://example.com")
    assertTrue(page!!.title().isNotBlank(), "example.com title should not be blank")

    page!!.navigate("$target/")
    assertEquals("$target/", page!!.url().removeSuffix("/"), "Final URL mismatch (normalized)")
  }
}
