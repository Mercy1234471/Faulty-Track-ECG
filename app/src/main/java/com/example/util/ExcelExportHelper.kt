package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.FaultEntity
import com.example.data.model.MeterRequestEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExportHelper {

    private fun escapeCsv(value: String?): String {
        if (value == null) return "\"\""
        val escaped = value.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ")
        return "\"$escaped\""
    }

    /**
     * Generates standard RFC 4180 CSV with UTF-8 BOM so Microsoft Excel
     * displays all characters cleanly without character set or column delimiter issues.
     */
    fun generateFaultsCsv(faults: List<FaultEntity>): String {
        val sb = StringBuilder()
        // UTF-8 BOM for automatic Excel delimiter & encoding detection
        sb.append("\uFEFF")
        // Headers
        sb.append("Reference Number,Fault Type,Region,District,Location Address,GPS Address,Description,Reported Date,Reported Time,Status,Assigned Technician,Technician Phone,Technician Location,Resolution Notes\n")

        for (f in faults) {
            sb.append(escapeCsv(f.faultReference)).append(",")
            sb.append(escapeCsv(f.faultType)).append(",")
            sb.append(escapeCsv(f.region)).append(",")
            sb.append(escapeCsv(f.district)).append(",")
            sb.append(escapeCsv(f.locationAddress)).append(",")
            sb.append(escapeCsv(f.gpsAddress)).append(",")
            sb.append(escapeCsv(f.description)).append(",")
            sb.append(escapeCsv(f.reportedDate)).append(",")
            sb.append(escapeCsv(f.reportedTime)).append(",")
            sb.append(escapeCsv(f.status)).append(",")
            sb.append(escapeCsv(f.assignedTechnicianName ?: "Unassigned")).append(",")
            sb.append(escapeCsv(f.assignedTechnicianPhone ?: "N/A")).append(",")
            sb.append(escapeCsv(f.assignedTechnicianLocation ?: "N/A")).append(",")
            sb.append(escapeCsv(f.resolutionNotes ?: "N/A")).append("\n")
        }
        return sb.toString()
    }

    fun generateMeterRequestsCsv(requests: List<MeterRequestEntity>): String {
        val sb = StringBuilder()
        sb.append("\uFEFF")
        sb.append("Request Reference,Applicant Name,Phone Number,GPS Address,Location,Region,Category,Status,Submitted Date,Submitted Time,Assigned Technician,Admin Remarks\n")

        for (r in requests) {
            sb.append(escapeCsv(r.requestReference)).append(",")
            sb.append(escapeCsv(r.fullName)).append(",")
            sb.append(escapeCsv(r.phoneNumber)).append(",")
            sb.append(escapeCsv(r.gpsAddress)).append(",")
            sb.append(escapeCsv(r.location)).append(",")
            sb.append(escapeCsv(r.region)).append(",")
            sb.append(escapeCsv(r.category)).append(",")
            sb.append(escapeCsv(r.status)).append(",")
            sb.append(escapeCsv(r.submittedDate)).append(",")
            sb.append(escapeCsv(r.submittedTime)).append(",")
            sb.append(escapeCsv(r.assignedTechnician ?: "Unassigned")).append(",")
            sb.append(escapeCsv(r.adminRemarks ?: "Pending review")).append("\n")
        }
        return sb.toString()
    }

    /**
     * Saves CSV file to device Downloads or App Documents and triggers an automatic Share/View Intent.
     */
    fun saveAndShareReport(
        context: Context,
        fileName: String,
        csvContent: String,
        title: String
    ): Result<String> {
        return try {
            val bytes = csvContent.toByteArray(Charsets.UTF_8)
            var fileUri: Uri? = null
            var savedPath = ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ECG_Reports")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os: OutputStream ->
                        os.write(bytes)
                        os.flush()
                    }
                    fileUri = uri
                    savedPath = "Downloads/ECG_Reports/$fileName"
                }
            }

            if (fileUri == null) {
                // Fallback to internal/external cache directory with FileProvider or direct share
                val exportDir = File(context.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val file = File(exportDir, fileName)
                FileOutputStream(file).use { os ->
                    os.write(bytes)
                    os.flush()
                }
                fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                savedPath = file.absolutePath
            }

            // Launch automatic view / share intent so user sees it downloaded immediately
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Exported ECG Excel Report: $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Download / Open $title").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

            Result.success("Exported successfully to $savedPath")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
