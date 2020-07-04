package io.github.simonschiller.permissioncheck.internal.report

import io.github.simonschiller.permissioncheck.internal.data.Permission
import io.github.simonschiller.permissioncheck.internal.data.Sdk23Permission
import io.github.simonschiller.permissioncheck.internal.data.Violation
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LogReporterTest {

    @Test
    fun `Success message is logged when there are no violations`() {
        val logger = TestLogger()
        val reporter = LogReporter(logger)

        val violations = emptyList<Violation>()
        reporter.report(mapOf("debug" to violations, "release" to violations))

        val expectedOutput = listOf(
            "Found no violations, all permissions match the baseline"
        )
        assertEquals(expectedOutput, logger.lifecycle)
    }

    @Test
    fun `Violations are logged if they exist`() {
        val logger = TestLogger()
        val reporter = LogReporter(logger)

        val violations = listOf(
            Violation.Added(Permission("android.permission.CAMERA")),
            Violation.Removed(Sdk23Permission("android.permission.ACCESS_COARSE_LOCATION")),
            Violation.MaxSdkIncreased(Permission("android.permission.ACCESS_FINE_LOCATION"), 27),
            Violation.MaxSdkDecreased(Sdk23Permission("android.permission.INTERNET", 25), 28)
        )
        reporter.report(mapOf("debug" to violations, "release" to emptyList()))

        val expectedOutput = listOf(
            "Found 4 violation(s) for variant debug",
            """New permission '<uses-permission android:name="android.permission.CAMERA" />' was added""",
            """Permission '<uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION" />' was removed""",
            """Max SDK of permission '<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />' was increased (from 27)""",
            """Max SDK of permission '<uses-permission-sdk-23 android:name="android.permission.INTERNET" android:maxSdkVersion="25" />' was decreased (from 28)"""
        )
        assertEquals(expectedOutput, logger.error)
    }

    @Test
    fun `Violations for multiple variants are logged separately`() {
        val logger = TestLogger()
        val reporter = LogReporter(logger)

        val debugViolations = listOf(
            Violation.Added(Permission("android.permission.CAMERA")),
            Violation.MaxSdkIncreased(Permission("android.permission.ACCESS_FINE_LOCATION"), 27)
        )
        val releaseViolations = listOf(
            Violation.Removed(Sdk23Permission("android.permission.ACCESS_COARSE_LOCATION")),
            Violation.MaxSdkDecreased(Sdk23Permission("android.permission.INTERNET", 25), 28)
        )
        reporter.report(mapOf("debug" to debugViolations, "release" to releaseViolations))

        val expectedOutput = listOf(
            "Found 2 violation(s) for variant debug",
            """New permission '<uses-permission android:name="android.permission.CAMERA" />' was added""",
            """Max SDK of permission '<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />' was increased (from 27)""",
            "Found 2 violation(s) for variant release",
            """Permission '<uses-permission-sdk-23 android:name="android.permission.ACCESS_COARSE_LOCATION" />' was removed""",
            """Max SDK of permission '<uses-permission-sdk-23 android:name="android.permission.INTERNET" android:maxSdkVersion="25" />' was decreased (from 28)"""
        )
        assertEquals(expectedOutput, logger.error)
    }

    private class TestLogger private constructor(logger: Logger) : Logger by logger {
        val lifecycle = mutableListOf<String>()
        val error = mutableListOf<String>()

        constructor() : this(Logging.getLogger(TestLogger::class.java))

        override fun lifecycle(message: String) {
            lifecycle += message
        }

        override fun error(message: String) {
            error += message
        }
    }
}
