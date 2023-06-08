package io.github.simonschiller.permissioncheck.internal

import io.github.simonschiller.permissioncheck.internal.data.BasePermission
import io.github.simonschiller.permissioncheck.internal.data.Feature
import io.github.simonschiller.permissioncheck.internal.data.Permission
import io.github.simonschiller.permissioncheck.internal.data.Sdk23Permission
import io.github.simonschiller.permissioncheck.internal.util.createDocumentBuilder
import io.github.simonschiller.permissioncheck.internal.util.forEach
import org.w3c.dom.Document
import java.io.File

internal class ManifestParser {
    private val namespace = "http://schemas.android.com/apk/res/android"

    fun parsePermissions(manifest: File): Set<BasePermission> {
        val permissions = mutableSetOf<BasePermission>()
        val document = createDocumentBuilder().parse(manifest)

        // Parse regular permissions
        permissions += parseNodes(document, "uses-permission").map { node ->
            Permission(node.name, node.maxSdkVersion)
        }

        // Parse SDK 23 permissions
        permissions += parseNodes(document, "uses-permission-sdk-23").map { node ->
            Sdk23Permission(node.name, node.maxSdkVersion)
        }

        // Parse features
        permissions += parseNodes(document, "uses-feature").map { node ->
            Feature(node.name, node.required, node.glEsVersion)
        }

        return permissions
    }

    private fun parseNodes(
        document: Document,
        tagName: String,
    ): Set<Node> {
        val nodes = mutableSetOf<Node>()
        val regularPermissions = document.getElementsByTagName(tagName)
        regularPermissions.forEach { element ->
            val name = element.getAttributeNS(namespace, "name")
            val maxSdkVersion = element.getAttributeNS(namespace, "maxSdkVersion").toIntOrNull()
            val required = element.getAttributeNS(namespace, "required").toBooleanStrictOrNull()
            val glEsVersion = element.getAttributeNS(namespace, "glEsVersion").takeIf { it.isNotEmpty() }
            nodes += Node(name, maxSdkVersion, required, glEsVersion)
        }
        return nodes.toSet()
    }

    data class Node(val name: String, val maxSdkVersion: Int?, val required: Boolean?, val glEsVersion: String?)
}
