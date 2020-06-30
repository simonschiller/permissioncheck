package io.github.simonschiller.permissioncheck.internal.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class PermissionsTest {

    @Test
    fun `Same types of permissions with same content are equal`() {
        val a = Permission("android.permission.INTERNET", 26)
        val b = Permission("android.permission.INTERNET", 26)
        assertEquals(a, b)
    }

    @Test
    fun `Different types of permissions with same content are not equal`() {
        val a = Permission("android.permission.INTERNET", 26)
        val b = Sdk23Permission("android.permission.INTERNET", 26)
        assertNotEquals(a, b)
    }

    @Test
    fun `XML representation is correct for normal permissions`() {
        val permission = Permission("android.permission.INTERNET")
        val xml = """<uses-permission android:name="android.permission.INTERNET" />"""
        assertEquals(xml, permission.toString())
    }

    @Test
    fun `XML representation is correct for normal permissions with max SDK`() {
        val permission = Permission("android.permission.INTERNET", 26)
        val xml = """<uses-permission android:name="android.permission.INTERNET" android:maxSdkVersion="26" />"""
        assertEquals(xml, permission.toString())
    }

    @Test
    fun `XML representation is correct for SDK 23 permissions`() {
        val permission = Sdk23Permission("android.permission.INTERNET")
        val xml = """<uses-permission-sdk-23 android:name="android.permission.INTERNET" />"""
        assertEquals(xml, permission.toString())
    }

    @Test
    fun `XML representation is correct for SDK 23 permissions with max SDK`() {
        val permission = Sdk23Permission("android.permission.INTERNET", 26)
        val xml = """<uses-permission-sdk-23 android:name="android.permission.INTERNET" android:maxSdkVersion="26" />"""
        assertEquals(xml, permission.toString())
    }

    @Test
    fun `Permissions are sorted by name and tag`() {
        val unsorted = listOf(
            Permission("android.permission.INTERNET"),
            Permission("android.permission.CAMERA"),
            Sdk23Permission("android.permission.CAMERA"),
            Sdk23Permission("android.permission.ACCESS_FINE_LOCATION")
        )

        val sorted = listOf(
            Sdk23Permission("android.permission.ACCESS_FINE_LOCATION"),
            Permission("android.permission.CAMERA"),
            Sdk23Permission("android.permission.CAMERA"),
            Permission("android.permission.INTERNET")
        )
        assertEquals(sorted, unsorted.sorted())
    }
}
