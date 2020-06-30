package io.github.simonschiller.permissioncheck.internal.data

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.*

internal abstract class BasePermission(val name: String, val maxSdkVersion: Int?) : Comparable<BasePermission> {
    abstract val tag: String

    fun toBaselineElement(document: Document): Element = document.createElement(tag).apply {
        setAttribute("name", name)
        if (maxSdkVersion != null) {
            setAttribute("maxSdkVersion", maxSdkVersion.toString())
        }
    }

    override fun toString(): String {
        val builder = StringBuilder("""<$tag android:name="$name" """)
        if (maxSdkVersion != null) {
            builder.append("""android:maxSdkVersion="$maxSdkVersion" """)
        }
        return builder.append("/>").toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BasePermission) return false
        return name == other.name && maxSdkVersion == other.maxSdkVersion && tag == other.tag
    }

    override fun hashCode(): Int {
        return Objects.hash(name, maxSdkVersion, tag)
    }

    override fun compareTo(other: BasePermission): Int {
        var result = name.compareTo(other.name)
        if (result == 0) {
            result = tag.compareTo(other.tag)
        }
        return result
    }
}

internal class Permission(name: String, maxSdkVersion: Int? = null) : BasePermission(name, maxSdkVersion) {
    override val tag: String = "uses-permission"
}

internal class Sdk23Permission(name: String, maxSdkVersion: Int? = null) : BasePermission(name, maxSdkVersion) {
    override val tag: String = "uses-permission-sdk-23"
}
