package uk.gov.justice.digital.hmpps.pecs.jpc.config

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import java.io.InputStream

class S3ResourceProvider(private val provider: AmazonS3, private val bucket: String) : ResourceProvider {
    override fun get(resourceName: String): InputStream = provider.getObject(GetObjectRequest(bucket, resourceName)).objectContent
}