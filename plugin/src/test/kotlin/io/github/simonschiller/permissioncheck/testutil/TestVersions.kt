package io.github.simonschiller.permissioncheck.testutil

import org.gradle.util.VersionNumber
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class TestVersions : ArgumentsProvider {

    // See https://gradle.org/releases
    private val gradleVersions = listOf("6.7.1", "6.6.1", "6.5.1", "6.4.1", "6.3", "6.2.2", "6.1.1")

    // See https://developer.android.com/studio/releases/gradle-plugin, with respective minimum Gradle version
    private val agpVersions = listOf(
        "4.1.1" to "6.5",
        "4.0.1" to "6.1.1"
    )

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        val arguments = agpVersions.flatMap { (agpVersion, minGradleVersion) ->
            val minVersion = VersionNumber.parse(minGradleVersion)
            gradleVersions
                .filter { gradleVersion -> VersionNumber.parse(gradleVersion) >= minVersion }
                .map { gradleVersion -> Arguments.of(gradleVersion, agpVersion) }
        }
        return arguments.stream()
    }

    companion object {
        const val LATEST_GRADLE_VERSION = "6.7.1"
        const val LATEST_AGP_VERSION = "4.1.1"
    }
}
