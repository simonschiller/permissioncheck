package io.github.simonschiller.permissioncheck.internal

import io.github.simonschiller.permissioncheck.internal.data.Permission
import io.github.simonschiller.permissioncheck.internal.data.Sdk23Permission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileNotFoundException

class ManifestParserTest {
    private val manifestParser = ManifestParser()

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `Permissions are parsed correctly`() {
        val manifest = tempDir.resolve("AndroidManifest.xml")
        manifest.writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="io.github.simonschiller.permissioncheck.test">

                <uses-sdk android:minSdkVersion="21" android:targetSdkVersion="29"/>

                <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
                <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="27"/>
                <uses-permission-sdk-23 android:name="android.permission.INTERNET"/>
                <uses-permission-sdk-23 android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="26"/>
            </manifest>
            
        """.trimIndent())

        val permissions = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION"),
            Permission("android.permission.ACCESS_FINE_LOCATION", 27),
            Sdk23Permission("android.permission.INTERNET"),
            Sdk23Permission("android.permission.WRITE_EXTERNAL_STORAGE", 26)
        )
        assertEquals(permissions, manifestParser.parsePermissions(manifest))
    }

    @Test
    fun `Exception is thrown if file does not exist`() {
        val manifest = tempDir.resolve("AndroidManifest.xml")
        assertThrows<FileNotFoundException> { manifestParser.parsePermissions(manifest) }
    }
}
