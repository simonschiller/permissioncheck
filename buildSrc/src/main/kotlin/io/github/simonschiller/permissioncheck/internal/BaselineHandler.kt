package io.github.simonschiller.permissioncheck.internal

import io.github.simonschiller.permissioncheck.internal.data.BasePermission
import io.github.simonschiller.permissioncheck.internal.data.Permission
import io.github.simonschiller.permissioncheck.internal.data.Sdk23Permission
import io.github.simonschiller.permissioncheck.internal.util.createDocumentBuilder
import io.github.simonschiller.permissioncheck.internal.util.createTransformer
import io.github.simonschiller.permissioncheck.internal.util.forEach
import java.io.File
import java.io.FileOutputStream
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

internal class BaselineHandler(private val baselineFile: File) {

    // Only XML baseline files are allowed
    init {
        if (!baselineFile.extension.equals("xml", ignoreCase = true)) {
            throw IllegalArgumentException("The permission baseline has to be a .xml file")
        }
    }

    fun serialize(variantName: String, permissions: Set<BasePermission>) {
        val baseline = deserialize().toMutableMap() // Parse existing baseline and add new or updated permissions
        baseline[variantName] = permissions

        // Package changes into a XML document
        val document = createDocumentBuilder().newDocument()
        val rootElement = document.createElement("baseline")
        baseline.forEach { (variantName, permissions) ->
            val variantElement = document.createElement("variant")
            variantElement.setAttribute("name", variantName)
            permissions.forEach { permission ->
                variantElement.appendChild(permission.toBaselineElement(document))
            }
            rootElement.appendChild(variantElement)
        }
        document.appendChild(rootElement)

        // Write updated baseline to disk
        baselineFile.parentFile.mkdirs()
        val transformer = createTransformer()
        FileOutputStream(baselineFile).use { outputStream ->
            transformer.transform(DOMSource(document), StreamResult(outputStream))
        }
    }

    fun deserialize(): Map<String, Set<BasePermission>> {
        val permissions = mutableMapOf<String, Set<BasePermission>>()

        // Missing files are interpreted as empty baselines
        if (!baselineFile.exists()) {
            return emptyMap()
        }

        // Parse the baseline file
        val document = try {
            createDocumentBuilder().parse(baselineFile)
        } catch (exception: Exception) {
            throw IllegalStateException("Could not parse baseline, please make sure the format is valid", exception)
        }

        document.getElementsByTagName("variant").forEach { variant ->
            val variantName = variant.getAttribute("name")
            val variantPermissions = mutableSetOf<BasePermission>()

            // Parse regular permissions for variant
            val regularPermissions = variant.getElementsByTagName("uses-permission")
            regularPermissions.forEach { element ->
                val name = element.getAttribute("name")
                val maxSdkVersion = element.getAttribute("maxSdkVersion").toIntOrNull()
                variantPermissions += Permission(name, maxSdkVersion)
            }

            // Parse SDK 23 permissions for variant
            val sdk23Permissions = variant.getElementsByTagName("uses-permission-sdk-23")
            sdk23Permissions.forEach { element ->
                val name = element.getAttribute("name")
                val maxSdkVersion = element.getAttribute("maxSdkVersion").toIntOrNull()
                variantPermissions += Sdk23Permission(name, maxSdkVersion)
            }

            permissions[variantName] = variantPermissions
        }

        return permissions
    }
}
