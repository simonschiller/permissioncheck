package io.github.simonschiller.permissioncheck.internal.data

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.*

internal abstract class BasePermission(val name: String, val maxSdkVersion: Int?, val required: Boolean?, val glEsVersion: String?) : Comparable<BasePermission> {
    abstract val tag: String

    fun toXmlElement(document: Document): Element = document.createElement(tag).apply {
        if (name.isNotEmpty()) {
            setAttribute("name", name)
        }
        if (maxSdkVersion != null) {
            setAttribute("maxSdkVersion", maxSdkVersion.toString())
        }
        if (required != null) {
            setAttribute("required", required.toString())
        }
        if (glEsVersion != null) {
            setAttribute("glEsVersion", glEsVersion)
        }
    }

    abstract fun copy(name: String = this.name, maxSdkVersion: Int? = this.maxSdkVersion, required: Boolean?, glEsVersion: String?): BasePermission

    override fun toString(): String {
        val builder = StringBuilder("""<$tag """)
        if (name.isNotEmpty()) {
            builder.append("""android:name="$name" """)
        }
        if (maxSdkVersion != null) {
            builder.append("""android:maxSdkVersion="$maxSdkVersion" """)
        }
        if (required != null) {
            builder.append("""android:required="$required" """)
        }
        if (glEsVersion != null) {
            builder.append("""android:glEsVersion="$glEsVersion" """)
        }
        return builder.append("/>").toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BasePermission) return false
        return name == other.name && maxSdkVersion == other.maxSdkVersion && required == other.required && glEsVersion == other.glEsVersion && tag == other.tag
    }

    override fun hashCode(): Int {
        return Objects.hash(name, maxSdkVersion, required, tag)
    }

    override fun compareTo(other: BasePermission): Int {
        var result = name.compareTo(other.name)
        if (result == 0) {
            result = tag.compareTo(other.tag)
        }
        return result
    }
}

internal class Permission(name: String, maxSdkVersion: Int? = null) : BasePermission(name, maxSdkVersion, null, null) {
    override val tag: String = "uses-permission"
    override fun copy(name: String, maxSdkVersion: Int?, required: Boolean?, glEsVersion: String?) = Permission(name, maxSdkVersion)
}

internal class Sdk23Permission(name: String, maxSdkVersion: Int? = null) : BasePermission(name, maxSdkVersion, null, null) {
    override val tag: String = "uses-permission-sdk-23"
    override fun copy(name: String, maxSdkVersion: Int?, required: Boolean?, glEsVersion: String?) = Sdk23Permission(name, maxSdkVersion)
}

internal class Feature(name: String, required: Boolean? = null, glEsVersion: String? = null) : BasePermission(name, null, required, glEsVersion) {
    override val tag: String = "uses-feature"
    override fun copy(name: String, maxSdkVersion: Int?, required: Boolean?, glEsVersion: String?) = Feature(name, required, glEsVersion)
}
