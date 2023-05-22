package io.github.simonschiller.permissioncheck.testutil

import org.gradle.util.VersionNumber
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class TestVersions : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        val arguments = AGP_VERSIONS.flatMap { agpVersion ->
            GRADLE_VERSIONS
                .filter { gradleVersion -> VersionNumber.parse(agpVersion).baseVersion isCompatibleWith VersionNumber.parse(gradleVersion).baseVersion }
                .map { gradleVersion -> Arguments.of(gradleVersion, agpVersion) }
        }
        return arguments.stream()
    }

    // Checks if a AGP version (receiver) is compatible with a certain version of Gradle
    private infix fun VersionNumber.isCompatibleWith(gradleVersion: VersionNumber) = when {
        this >= VersionNumber.parse("7.0.0") -> gradleVersion >= VersionNumber.parse("7.0")
        else -> false
    }

    companion object {

        // See https://gradle.org/releases
        private val GRADLE_VERSIONS = listOf(
            "7.4.2",
            "7.2",
        )

        // See https://developer.android.com/studio/releases/gradle-plugin
        private val AGP_VERSIONS = listOf(
            "7.0.0",
        )

        val LATEST_GRADLE_VERSION = GRADLE_VERSIONS.first()
        val LATEST_AGP_VERSION = AGP_VERSIONS.first()
    }
}
