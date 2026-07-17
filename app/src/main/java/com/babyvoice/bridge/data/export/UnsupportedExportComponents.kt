package com.babyvoice.bridge.data.export

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultExportFormatDetector @Inject constructor() : ExportFormatDetector {
    override fun detect(source: ImportSource, headerBytes: ByteArray): ExportFormat {
        val name = source.displayName.orEmpty().lowercase()
        val mime = source.mimeType.orEmpty().lowercase()
        return when {
            name.endsWith(".zip") || mime.contains("zip") -> ExportFormat.ZIP
            name.endsWith(".txt") || mime.startsWith("text/") -> ExportFormat.TEXT
            else -> ExportFormat.UNSUPPORTED
        }
    }
}

@Singleton
class UnsupportedExportRecordParser @Inject constructor() : ExportRecordParser {
    override fun canParse(format: ExportFormat): Boolean = format != ExportFormat.UNSUPPORTED

    override fun parse(source: ImportSource, payload: ByteArray): Result<List<ImportedExport>> =
        Result.failure(IllegalStateException("unsupported_format"))
}

