package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Klaxon
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Move

class MovesParserTest {

    @Test
    fun `Assert Move can be created from json`() {
        val moves = getReportLines("/reporting/moves.jsonl")

        val expectedMove = Move(id="02b4c0f5-4d85-4fb6-be6c-53d74b85bf2e", reference="UKW4591N", date="2021-02-28", status="requested", fromLocation="WYI", toLocation="GNI")
        val parsedMode = Move.fromJson(moves[0])

        Assertions.assertEquals(expectedMove, parsedMode)
    }


}