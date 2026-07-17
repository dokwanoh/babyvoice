package com.babyvoice.bridge.data.export

import android.net.Uri
import com.babyvoice.bridge.core.model.BabyCareSnapshot
import com.babyvoice.bridge.core.model.BabyId
import java.time.Instant

enum class ExportFormat {
    ZIP,
    TEXT,
    UNSUPPORTED,
}

data class ImportSource(
    val uri: Uri,
    val displayName: String?,
    val mimeType: String?,
)

data class ImportedExport(
    val babyId: BabyId,
    val snapshot: BabyCareSnapshot,
    val importedAt: Instant,
    val sourceDescription: String,
)

interface ExportFormatDetector {
    fun detect(source: ImportSource, headerBytes: ByteArray = ByteArray(0)): ExportFormat
}

interface ExportRecordParser {
    fun canParse(format: ExportFormat): Boolean
    fun parse(source: ImportSource, payload: ByteArray): Result<List<ImportedExport>>
}

interface ExportImportCoordinator {
    suspend fun import(source: ImportSource): Result<List<ImportedExport>>
}

