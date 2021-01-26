package io.github.simonschiller.permissioncheck.testutil

import org.gradle.util.VersionNumber
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class TestVersions : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        val arguments = AGP_VERSIONS.flatMap { (agpVersion, minGradleVersion) ->
            val minVersion = VersionNumber.parse(minGradleVersion)
            GRADLE_VERSIONS
                .filter { gradleVersion -> VersionNumber.parse(gradleVersion) >= minVersion }
                .map { gradleVersion -> Arguments.of(gradleVersion, agpVersion) }
        }
        return arguments.stream()
    }

    companion object {

        // See https://gradle.org/releases
        private val GRADLE_VERSIONS = listOf("6.8.1", "6.7.1", "6.6.1", "6.5.1", "6.4.1", "6.3", "6.2.2", "6.1.1")

        // See https://developer.android.com/studio/releases/gradle-plugin, with respective minimum Gradle version
        private val AGP_VERSIONS = listOf(
            "4.2.0-beta03" to "6.7.1",
            "4.1.2" to "6.5",
            "4.0.2" to "6.1.1"
        )

        val LATEST_GRADLE_VERSION = GRADLE_VERSIONS.first()
        val LATEST_AGP_VERSION = AGP_VERSIONS.first().first
    }
}
