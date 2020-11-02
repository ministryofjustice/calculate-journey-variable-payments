package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.crypt.EncryptionMode
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.File
import java.io.FileOutputStream

internal class POISpreadsheetProtection(private val value: String) : SpreadsheetProtection {
    override fun protectAndGet(file: File): File {
        POIFSFileSystem().use { pfs ->

            EncryptionInfo(EncryptionMode.agile).encryptor.let { encryptor ->

                OPCPackage.open(file, PackageAccess.READ_WRITE).let { opc ->
                    encryptor.confirmPassword(value)
                    encryptor.getDataStream(pfs).use { os -> opc.save(os) }
                }

                FileOutputStream(file).use { fos -> pfs.writeFilesystem(fos) }
            }
        }

        return file
    }
}