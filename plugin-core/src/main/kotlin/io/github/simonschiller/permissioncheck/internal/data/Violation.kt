package io.github.simonschiller.permissioncheck.internal.data

import org.w3c.dom.Document
import org.w3c.dom.Element

internal sealed class Violation {
    abstract val permission: BasePermission
    abstract val message: String
    open val strict: Boolean = false

    // For XML report generation
    abstract val type: String

    // For HTML report generation
    abstract val title: String
    abstract val description: String

    open fun toXmlElement(document: Document): Element = document.createElement("violation").apply {
        setAttribute("type", type)
        if (strict) {
            setAttribute("strict", strict.toString())
        }
        appendChild(permission.toXmlElement(document))
    }

    data class Added(override val permission: BasePermission) : Violation() {
        override val message = "New permission '$permission' was added"

        override val type = "added"

        override val title = "New permission added"
        override val description = "Permission was found in the manifest, but not in the baseline."
    }

    data class Removed(override val permission: BasePermission) : Violation() {
        override val message = "Permission '$permission' was removed"
        override val strict = true

        override val type = "removed"

        override val title = "Permission removed"
        override val description = "Permission was found in the baseline, but not in the manifest."
    }

    data class MaxSdkIncreased(override val permission: BasePermission, val from: Int?) : Violation() {
        override val message = "Max SDK of permission '$permission' was increased (from ${from ?: "-"})"

        override val type = "max-sdk-increased"

        override val title = "Max SDK increased"
        override val description = "Manifest contains a lower max SDK for permission than the baseline entry."

        override fun toXmlElement(document: Document) = super.toXmlElement(document).apply {
            setAttribute("previousMaxSdkVersion", from?.toString() ?: "-")
        }
    }

    data class MaxSdkDecreased(override val permission: BasePermission, val from: Int?) : Violation() {
        override val message = "Max SDK of permission '$permission' was decreased (from ${from ?: "-"})"
        override val strict = true

        override val type = "max-sdk-decreased"

        override val title = "Max SDK decreased"
        override val description = "Manifest contains a higher max SDK for permission than the baseline entry."

        override fun toXmlElement(document: Document) = super.toXmlElement(document).apply {
            setAttribute("previousMaxSdkVersion", from?.toString() ?: "-")
        }
    }

    data class RequiredChanged(override val permission: BasePermission, val required: Boolean?) : Violation() {
        override val message = "Required of '$permission' has changed (required ${required ?: "-"})"

        override val type = "required-changed"

        override val title = "Required changed"
        override val description = "Required has changed."

        override fun toXmlElement(document: Document) = super.toXmlElement(document).apply {
            setAttribute("previousRequired", required?.toString() ?: "-")
        }
    }

    data class GlEsVersionChanged(override val permission: BasePermission, val glEsVersion: String?) : Violation() {
        override val message = "GlEsVersion of '$permission' has changed (glEsVersion ${glEsVersion ?: "-"})"

        override val type = "glEsVersion-changed"

        override val title = "glEsVersion changed"
        override val description = "glEsVersion has changed."

        override fun toXmlElement(document: Document) = super.toXmlElement(document).apply {
            setAttribute("previousRequired", glEsVersion?.toString() ?: "-")
        }
    }
}
