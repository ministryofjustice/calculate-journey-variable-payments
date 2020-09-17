package uk.gov.justice.digital.hmpps.pecs.jpc.config

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import java.io.InputStream

class S3SpreadsheetProvider(private val provider: AmazonS3, private val bucket: String) : SpreadsheetProvider {
    override fun get(resourceName: String): InputStream = provider.getObject(GetObjectRequest(bucket, resourceName)).objectContent
}