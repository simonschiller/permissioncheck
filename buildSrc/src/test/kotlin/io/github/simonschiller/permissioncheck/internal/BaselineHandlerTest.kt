package io.github.simonschiller.permissioncheck.internal

import io.github.simonschiller.permissioncheck.internal.data.Permission
import io.github.simonschiller.permissioncheck.internal.data.Sdk23Permission
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BaselineHandlerTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `Baseline file is created during serialization, if it does not exist yet`() {
        val baselineFile = tempDir.resolve("permission-baseline.xml")

        val permissions = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION"),
            Permission("android.permission.ACCESS_FINE_LOCATION", 27),
            Sdk23Permission("android.permission.INTERNET"),
            Sdk23Permission("android.permission.WRITE_EXTERNAL_STORAGE", 26)
        )

        val baselineHandler = BaselineHandler(baselineFile)
        baselineHandler.serialize(mapOf("debug" to permissions))

        val expectedBaselineContent = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.ACCESS_COARSE_LOCATION"/>
                    <uses-permission maxSdkVersion="27" name="android.permission.ACCESS_FINE_LOCATION"/>
                    <uses-permission-sdk-23 name="android.permission.INTERNET"/>
                    <uses-permission-sdk-23 maxSdkVersion="26" name="android.permission.WRITE_EXTERNAL_STORAGE"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(expectedBaselineContent, baselineFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Baseline file is extended during serialization, if it already exists`() {
        val baselineFile = tempDir.resolve("permission-baseline.xml")
        baselineFile.writeText("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="release">
                    <uses-permission maxSdkVersion="27" name="android.permission.ACCESS_FINE_LOCATION"/>
                    <uses-permission-sdk-23 maxSdkVersion="26" name="android.permission.WRITE_EXTERNAL_STORAGE"/>
                </variant>
            </baseline>
            
        """.trimIndent())

        val permissions = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION"),
            Sdk23Permission("android.permission.INTERNET")
        )

        val baselineHandler = BaselineHandler(baselineFile)
        baselineHandler.serialize(mapOf("debug" to permissions))

        val expectedBaselineContent = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="release">
                    <uses-permission maxSdkVersion="27" name="android.permission.ACCESS_FINE_LOCATION"/>
                    <uses-permission-sdk-23 maxSdkVersion="26" name="android.permission.WRITE_EXTERNAL_STORAGE"/>
                </variant>
                <variant name="debug">
                    <uses-permission name="android.permission.ACCESS_COARSE_LOCATION"/>
                    <uses-permission-sdk-23 name="android.permission.INTERNET"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(expectedBaselineContent, baselineFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Baseline entry is overridden during serialization, if it already exists`() {
        val baselineFile = tempDir.resolve("permission-baseline.xml")
        baselineFile.writeText("""
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.ACCESS_COARSE_LOCATION"/>
                    <uses-permission-sdk-23 name="android.permission.INTERNET"/>
                </variant>
            </baseline>
            
        """.trimIndent())

        val permissions = setOf(
            Permission("android.permission.ACCESS_FINE_LOCATION", 27),
            Sdk23Permission("android.permission.WRITE_EXTERNAL_STORAGE", 26)
        )

        val baselineHandler = BaselineHandler(baselineFile)
        baselineHandler.serialize(mapOf("debug" to permissions))

        val expectedBaselineContent = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission maxSdkVersion="27" name="android.permission.ACCESS_FINE_LOCATION"/>
                    <uses-permission-sdk-23 maxSdkVersion="26" name="android.permission.WRITE_EXTERNAL_STORAGE"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(expectedBaselineContent, baselineFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Baseline is overridden, if existing baseline file is corrupt`() {
        val baselineFile = tempDir.resolve("permission-baseline.xml")
        baselineFile.writeText("""
            <foo>
                <bar>Not a valid baseline</bar>
            </foo>
        """.trimIndent())

        val permissions = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION"),
            Sdk23Permission("android.permission.INTERNET")
        )

        val baselineHandler = BaselineHandler(baselineFile)
        baselineHandler.serialize(mapOf("debug" to permissions))

        val expectedBaselineContent = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.ACCESS_COARSE_LOCATION"/>
                    <uses-permission-sdk-23 name="android.permission.INTERNET"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(expectedBaselineContent, baselineFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Baseline file with multiple variants is created correctly`() {
        val baselineFile = tempDir.resolve("permission-baseline.xml")

        val debugPermissions = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION"),
            Sdk23Permission("android.permission.INTERNET")
        )

        val releasePermissions = setOf(
            Permission("android.permission.ACCESS_FINE_LOCATION", 27),
            Sdk23Permission("android.permission.WRITE_EXTERNAL_STORAGE", 26)
        )

        val baselineHandler = BaselineHandler(baselineFile)
        baselineHandler.serialize(mapOf("debug" to debugPermissions, "release" to releasePermissions))

        val expectedBaselineContent = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.ACCESS_COARSE_LOCATION"/>
                    <uses-permission-sdk-23 name="android.permission.INTERNET"/>
                </variant>
                <variant name="release">
                    <uses-permission maxSdkVersion="27" name="android.permission.ACCESS_FINE_LOCATION"/>
                    <uses-permission-sdk-23 maxSdkVersion="26" name="android.permission.WRITE_EXTERNAL_STORAGE"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(expectedBaselineContent, baselineFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Baseline is deserialized correctly`() {
        val baselineFile = tempDir.resolve("permission-baseline.xml")
        baselineFile.writeText("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.ACCESS_COARSE_LOCATION"/>
                    <uses-permission-sdk-23 name="android.permission.INTERNET"/>
                </variant>
                <variant name="release">
                    <uses-permission maxSdkVersion="27" name="android.permission.ACCESS_FINE_LOCATION"/>
                    <uses-permission-sdk-23 maxSdkVersion="26" name="android.permission.WRITE_EXTERNAL_STORAGE"/>
                </variant>
            </baseline>
            
        """.trimIndent())

        val baselineHandler = BaselineHandler(baselineFile)
        val baseline = baselineHandler.deserialize()

        val debugPermissions = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION"),
            Sdk23Permission("android.permission.INTERNET")
        )
        assertEquals(debugPermissions, baseline["debug"])

        val releasePermissions = setOf(
            Permission("android.permission.ACCESS_FINE_LOCATION", 27),
            Sdk23Permission("android.permission.WRITE_EXTERNAL_STORAGE", 26)
        )
        assertEquals(releasePermissions, baseline["release"])
    }

    @Test
    fun `Non-existent baseline file is interpreted as empty`() {
        val baselineFile = tempDir.resolve("permission-baseline.xml")

        val baselineHandler = BaselineHandler(baselineFile)
        val baseline = baselineHandler.deserialize()

        assertTrue(baseline.isEmpty())
    }

    @Test
    fun `Exception is thrown if baseline file has wrong type`() {
        val baselineFile = tempDir.resolve("permission-baseline.json")
        assertThrows<IllegalArgumentException> { BaselineHandler(baselineFile) }
    }
}
