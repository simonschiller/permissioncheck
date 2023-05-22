package io.github.simonschiller.permissioncheck.config

import org.gradle.util.VersionNumber

/** Factory that produces different [TaskConfigurator]s to maintain backwards compatibility to older AGP versions. */
internal object TaskConfiguratorFactory {

    fun getTaskConfigurator(): TaskConfigurator {
        val version = VersionNumber.parse(getAndroidGradlePluginVersion())
        return when {
            else -> TaskConfiguratorV1()
        }
    }

    private fun getAndroidGradlePluginVersion(): String {
        val version = Class.forName("com.android.Version")
        val field = version.getField("ANDROID_GRADLE_PLUGIN_VERSION")
        return field.get(null) as String
    }
}
