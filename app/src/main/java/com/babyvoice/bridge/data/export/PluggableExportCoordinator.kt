package com.babyvoice.bridge.data.export

import android.content.ContentResolver
import java.io.ByteArrayOutputStream

class PluggableExportImportCoordinator(
    private val contentResolver: ContentResolver,
    private val formatDetector: ExportFormatDetector,
    private val parsers: Set<@JvmSuppressWildcards ExportRecordParser>,
    private val zipReader: SafeZipReader,
) : ExportImportCoordinator {
    override suspend fun import(source: ImportSource): Result<List<ImportedExport>> = runCatching {
        val payload = contentResolver.openInputStream(source.uri)?.use { input ->
            ByteArrayOutputStream().use { output ->
                input.copyTo(output)
                output.toByteArray()
            }
        } ?: throw IllegalArgumentException("missing_input_stream")

        val format = formatDetector.detect(source, payload.copyOfRange(0, minOf(8, payload.size)))
        val parser = parsers.firstOrNull { it.canParse(format) } ?: throw IllegalArgumentException("unsupported_format")
        if (format == ExportFormat.ZIP) {
            zipReader.read(payload)
        }
        parser.parse(source, payload).getOrThrow()
    }
}
