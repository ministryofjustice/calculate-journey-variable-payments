package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.KlaxonException
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocationConverter(val locations: Collection<Location>): Converter {
    private val agencyId2Location = locations.associateBy(Location::nomisAgencyId)

    override fun canConvert(cls: Class<*>)
            = cls == Location::class.java

    override fun fromJson(jv: JsonValue) =
            jv.string?.let{agencyId2Location[jv.string] ?: Location(LocationType.UNKNOWN, jv.string!!, jv.string!!)}


    override fun toJson(o: Any)
            = """ { "location" : $o } """
}