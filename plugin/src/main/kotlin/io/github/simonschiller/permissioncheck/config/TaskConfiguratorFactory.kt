package io.github.simonschiller.permissioncheck.config

import org.gradle.util.VersionNumber

/** Factory that produces different [TaskConfigurator]s to maintain backwards compatibility to older AGP versions. */
internal object TaskConfiguratorFactory {

    fun getTaskConfigurator(): TaskConfigurator {
        val version = VersionNumber.parse(getAndroidGradlePluginVersion())
        return when {
            version <= VersionNumber.parse("4.0.2") -> TaskConfiguratorV1()
            version <= VersionNumber.parse("4.1.2") -> TaskConfiguratorV2()
            version <= VersionNumber.parse("4.2.2") -> TaskConfiguratorV3()
            else -> TaskConfiguratorV4()
        }
    }

    private fun getAndroidGradlePluginVersion(): String {
        val version = Class.forName("com.android.Version")
        val field = version.getField("ANDROID_GRADLE_PLUGIN_VERSION")
        return field.get(null) as String
    }
}
