package io.github.simonschiller.permissioncheck.internal.config

import com.android.Version
import org.gradle.util.VersionNumber

/** Factory that produces different [TaskConfigurator]s to maintain backwards compatibility to older AGP versions. */
internal object TaskConfiguratorFactory {

    fun getTaskConfigurator(): TaskConfigurator {
        val version = VersionNumber.parse(Version.ANDROID_GRADLE_PLUGIN_VERSION)
        return when {
            version <= VersionNumber.parse("4.0.1") -> TaskConfiguratorV1()
            else -> TaskConfiguratorV2()
        }
    }
}
