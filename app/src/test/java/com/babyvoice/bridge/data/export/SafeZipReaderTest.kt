package com.babyvoice.bridge.data.export

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeZipReaderTest {
    private val reader = SafeZipReader(maxEntries = 4, maxUncompressedBytes = 1024)

    @Test
    fun readsSafeArchiveEntries() {
        val payload = zipBytes(
            "notes/export.txt" to "hello".toByteArray(),
            "nested/data.json" to """{"ok":true}""".toByteArray(),
        )

        val entries = reader.read(payload).getOrThrow()

        assertEquals(2, entries.size)
        assertEquals("notes/export.txt", entries[0].path)
        assertEquals("hello", entries[0].bytes.decodeToString())
        assertEquals("nested/data.json", entries[1].path)
    }

    @Test
    fun rejectsZipSlipPaths() {
        val payload = zipBytes("../evil.txt" to "nope".toByteArray())

        val result = reader.read(payload)

        assertTrue(result.isFailure)
    }

    @Test
    fun rejectsMalformedArchive() {
        val result = reader.read(zipBytes("notes/export.txt" to "hello".toByteArray()).copyOfRange(0, 20))
        assertTrue(result.isFailure)
    }

    private fun zipBytes(vararg entries: Pair<String, ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }
}
