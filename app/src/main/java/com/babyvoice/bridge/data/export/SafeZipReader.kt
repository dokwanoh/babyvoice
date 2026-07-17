package com.babyvoice.bridge.data.export

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

data class SafeZipEntry(
    val path: String,
    val bytes: ByteArray,
)

class SafeZipReader(
    private val maxEntries: Int = 32,
    private val maxUncompressedBytes: Int = 5 * 1024 * 1024,
) {
    fun read(payload: ByteArray): Result<List<SafeZipEntry>> = runCatching {
        ByteArrayInputStream(payload).use { input ->
            read(input)
        }
    }

    fun read(inputStream: InputStream): List<SafeZipEntry> {
        val payload = inputStream.readBytes()
        ensureLooksLikeCompleteZip(payload)
        ZipInputStream(ByteArrayInputStream(payload)).use { zip ->
            val entries = mutableListOf<SafeZipEntry>()
            var totalBytes = 0
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.isDirectory) {
                    zip.closeEntry()
                    continue
                }
                if (entries.size >= maxEntries) {
                    throw IllegalArgumentException("too_many_entries")
                }
                val normalized = normalizeEntryPath(entry.name)
                val bytes = zip.readBytes(maxUncompressedBytes - totalBytes)
                totalBytes += bytes.size
                if (totalBytes > maxUncompressedBytes) {
                    throw IllegalArgumentException("archive_too_large")
                }
                entries += SafeZipEntry(normalized, bytes)
                zip.closeEntry()
            }
            return entries
        }
    }

    private fun ensureLooksLikeCompleteZip(payload: ByteArray) {
        val eocdSignature = byteArrayOf(0x50, 0x4b, 0x05, 0x06)
        if (!payload.containsSequence(eocdSignature)) {
            throw IllegalArgumentException("malformed_zip")
        }
    }

    private fun normalizeEntryPath(rawPath: String): String {
        if (rawPath.startsWith("/") || rawPath.contains(":")) {
            throw IllegalArgumentException("zip_slip_detected")
        }
        val segments = rawPath.split('/', '\\')
            .filter { it.isNotBlank() && it != "." }
        if (segments.isEmpty()) {
            throw IllegalArgumentException("zip_slip_detected")
        }
        if (segments.any { it == ".." }) {
            throw IllegalArgumentException("zip_slip_detected")
        }
        return segments.joinToString("/")
    }

    private fun ByteArray.containsSequence(sequence: ByteArray): Boolean {
        if (sequence.isEmpty() || size < sequence.size) return false
        return indices.any { start ->
            start + sequence.size <= size && sequence.indices.all { offset -> this[start + offset] == sequence[offset] }
        }
    }

    private fun ZipInputStream.readBytes(maxBytes: Int): ByteArray {
        if (maxBytes <= 0) {
            throw IllegalArgumentException("archive_too_large")
        }
        val buffer = ByteArrayOutputStream()
        val chunk = ByteArray(DEFAULT_BUFFER_SIZE)
        var remaining = maxBytes
        while (true) {
            val read = read(chunk)
            if (read == -1) break
            remaining -= read
            if (remaining < 0) {
                throw IllegalArgumentException("entry_too_large")
            }
            buffer.write(chunk, 0, read)
        }
        return buffer.toByteArray()
    }
}
