package io.github.simonschiller.permissioncheck.internal

import io.github.simonschiller.permissioncheck.internal.data.BasePermission
import io.github.simonschiller.permissioncheck.internal.data.Permission
import io.github.simonschiller.permissioncheck.internal.data.Sdk23Permission
import io.github.simonschiller.permissioncheck.internal.util.createDocumentBuilder
import io.github.simonschiller.permissioncheck.internal.util.forEach
import java.io.File

internal class ManifestParser {
    private val namespace = "http://schemas.android.com/apk/res/android"

    fun parsePermissions(manifest: File): Set<BasePermission> {
        val permissions = mutableSetOf<BasePermission>()
        val document = createDocumentBuilder().parse(manifest)

        // Parse regular permissions
        val regularPermissions = document.getElementsByTagName("uses-permission")
        regularPermissions.forEach { element ->
            val name = element.getAttributeNS(namespace, "name")
            val maxSdkVersion = element.getAttributeNS(namespace, "maxSdkVersion").toIntOrNull()
            permissions += Permission(name, maxSdkVersion)
        }

        // Parse SDK 23 permissions
        val sdk23Permissions = document.getElementsByTagName("uses-permission-sdk-23")
        sdk23Permissions.forEach { element ->
            val name = element.getAttributeNS(namespace, "name")
            val maxSdkVersion = element.getAttributeNS(namespace, "maxSdkVersion").toIntOrNull()
            permissions += Sdk23Permission(name, maxSdkVersion)
        }

        return permissions
    }
}
