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
                .filter { gradleVersion -> VersionNumber.parse(agpVersion) isCompatibleWith VersionNumber.parse(gradleVersion) }
                .map { gradleVersion -> Arguments.of(gradleVersion, agpVersion) }
        }
        return arguments.stream()
    }

    // Checks if a AGP version (receiver) is compatible with a certain version of Gradle
    private infix fun VersionNumber.isCompatibleWith(gradleVersion: VersionNumber) = when {
        this >= VersionNumber.parse("4.2.0") -> gradleVersion >= VersionNumber.parse("6.7.1")
        this >= VersionNumber.parse("4.1.0") -> gradleVersion >= VersionNumber.parse("6.5")
        this >= VersionNumber.parse("4.0.0") -> gradleVersion >= VersionNumber.parse("6.1.1") && gradleVersion < VersionNumber.parse("7.0")
        else -> false
    }

    companion object {

        // See https://gradle.org/releases
        private val GRADLE_VERSIONS = listOf(
            "7.0",
            "6.8.3",
            "6.7.1",
            "6.6.1",
            "6.5.1",
            "6.4.1",
            "6.3",
            "6.2.2",
            "6.1.1"
        )

        // See https://developer.android.com/studio/releases/gradle-plugin
        private val AGP_VERSIONS = listOf(
            "4.2.0",
            "4.1.2",
            "4.0.2"
        )

        val LATEST_GRADLE_VERSION = GRADLE_VERSIONS.first()
        val LATEST_AGP_VERSION = AGP_VERSIONS.first()
    }
}
