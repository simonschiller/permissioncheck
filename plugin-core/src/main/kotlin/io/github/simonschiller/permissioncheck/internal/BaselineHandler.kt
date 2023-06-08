package io.github.simonschiller.permissioncheck.internal

import io.github.simonschiller.permissioncheck.internal.data.BasePermission
import io.github.simonschiller.permissioncheck.internal.data.Feature
import io.github.simonschiller.permissioncheck.internal.data.Permission
import io.github.simonschiller.permissioncheck.internal.data.Sdk23Permission
import io.github.simonschiller.permissioncheck.internal.util.appendElement
import io.github.simonschiller.permissioncheck.internal.util.createDocumentBuilder
import io.github.simonschiller.permissioncheck.internal.util.forEach
import io.github.simonschiller.permissioncheck.internal.util.writeToFile
import java.io.File

internal class BaselineHandler(private val baselineFile: File) {

    // Only XML baseline files are allowed
    init {
        if (!baselineFile.extension.equals("xml", ignoreCase = true)) {
            throw IllegalArgumentException("The permission baseline has to be a .xml file")
        }
    }

    fun serialize(permissions: Map<String, Set<BasePermission>>) {
        val baseline = deserialize().toMutableMap() // Parse existing baseline and add new or updated permissions
        permissions.forEach { (variantName, variantPermissions) ->
            baseline[variantName] = variantPermissions
        }

        // Package changes into a XML document
        val document = createDocumentBuilder().newDocument()
        document.appendElement("baseline") {
            baseline.forEach { (variantName, variantPermissions) ->

                // Create permission entries for a single variant
                appendElement("variant") {
                    setAttribute("name", variantName)
                    variantPermissions.forEach { permission ->
                        appendChild(permission.toXmlElement(document))
                    }
                }
            }
        }

        // Write updated baseline to disk
        document.writeToFile(baselineFile)
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

            val features = variant.getElementsByTagName("uses-feature")
            features.forEach { element ->
                val name = element.getAttribute("name")
                val required = element.getAttribute("required").toBooleanStrictOrNull()
                val glEsVersion = element.getAttribute("glEsVersion").takeIf { it.isNotEmpty() }
                variantPermissions += Feature(name, required, glEsVersion)
            }

            permissions[variantName] = variantPermissions
        }

        return permissions
    }
}
