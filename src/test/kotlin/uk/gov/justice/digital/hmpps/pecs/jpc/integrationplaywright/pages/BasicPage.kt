package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import java.util.*

open class BasicPage {

  private var properties: Properties = Properties()

  init {
    properties.load(this::class.java.getResourceAsStream("/application-playwright.properties"))
  }

  fun getProperty(name: String): String {
    return properties.getProperty(name)
  }
}