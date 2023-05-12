package io.github.simonschiller.permissioncheck

import io.github.simonschiller.permissioncheck.testutil.AndroidProjectExtension
import io.github.simonschiller.permissioncheck.testutil.TestVersions
import io.github.simonschiller.permissioncheck.testutil.outcomeOf
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class SingleVariantPermissionCheckIntegrationTest {

    @JvmField
    @RegisterExtension
    val androidProject = AndroidProjectExtension()

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Baseline file is created if it does not exist yet`(gradleVersion: String, agpVersion: String) {
        val buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion,
            expectFailure = true
        )
        assertTrue(buildResult.output.contains("Created baseline"))
        assertTrue(buildResult.output.contains("Aborting build since new baseline was created"))

        val baseline = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                    <uses-feature name="android.hardware.camera.autofocus" required="false"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(baseline, androidProject.baselineFile.readText().normaliseLineSeparators())
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Baseline file is extended for new variants`(gradleVersion: String, agpVersion: String) {
        androidProject.baselineFile.writeText("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="release">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
            </baseline>
        """.trimIndent())

        val buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion,
            expectFailure = true
        )
        assertTrue(buildResult.output.contains("Created baseline"))
        assertTrue(buildResult.output.contains("Aborting build since new baseline was created"))

        val baseline = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="release">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
                <variant name="debug">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                    <uses-feature name="android.hardware.camera.autofocus" required="false"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(baseline, androidProject.baselineFile.readText().normaliseLineSeparators())
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Task completes without failure when recreating baselines`(gradleVersion: String, agpVersion: String) {
        val buildResult = androidProject.runTask(
            "checkDebugPermissions",
            "--recreate",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertTrue(buildResult.output.contains("Created baseline"))

        val baseline = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                    <uses-feature name="android.hardware.camera.autofocus" required="false"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(baseline, androidProject.baselineFile.readText().normaliseLineSeparators())
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Task succeeds silently if there are no violations`(gradleVersion: String, agpVersion: String) {
        androidProject.setupBaseline()
        val buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertTrue(buildResult.output.contains("Found no violations"))
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Task fails if there are violations`(gradleVersion: String, agpVersion: String) {
        androidProject.setupBaselineWithViolations()
        val buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion,
            expectFailure = true
        )
        assertTrue(buildResult.output.contains("Found 3 violation(s)"))
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Task fails if there are strict mode violations and strict mode is enabled via command line`(gradleVersion: String, agpVersion: String) {
        androidProject.setupBaselineWithViolations()
        val buildResult = androidProject.runTask(
            "checkDebugPermissions",
            "--strict",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion,
            expectFailure = true
        )
        assertTrue(buildResult.output.contains("Found 4 violation(s)"))
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Task fails if there are strict mode violations and strict mode is enabled via configuration`(gradleVersion: String, agpVersion: String) {
        val buildGradleFile = androidProject.appDir.resolve("build.gradle")
        buildGradleFile.appendText("""
            permissionCheck {
                strict.set(true)
            }
        """.trimIndent())

        androidProject.setupBaselineWithViolations()
        val buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion,
            expectFailure = true
        )
        assertTrue(buildResult.output.contains("Found 4 violation(s)"))
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Configuring a different baseline location works correctly`(gradleVersion: String, agpVersion: String) {
        val buildGradleFile = androidProject.appDir.resolve("build.gradle")
        buildGradleFile.appendText("""
            permissionCheck {
                baselineFile.set(layout.projectDirectory.file("baselines/test-baseline.xml"))
            }
        """.trimIndent())

        val buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion,
            expectFailure = true
        )

        val baselineFile = androidProject.appDir.resolve("baselines").resolve("test-baseline.xml")
        assertTrue(buildResult.output.contains(baselineFile.path))
        assertTrue(baselineFile.exists())
        assertFalse(androidProject.appDir.resolve("permission-baseline.xml").exists())
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Reports are generated when task is executed`(gradleVersion: String, agpVersion: String) {
        androidProject.setupBaseline()
        androidProject.runTask("checkDebugPermissions", gradleVersion = gradleVersion, agpVersion = agpVersion)

        val reportDirectory = androidProject.appDir.resolve("build").resolve("reports").resolve("permissioncheck")
        assertTrue(reportDirectory.resolve("permission-check-report-debug.xml").exists())
        assertTrue(reportDirectory.resolve("permission-check-report-debug.html").exists())
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Configuring a different report location works correctly`(gradleVersion: String, agpVersion: String) {
        val buildGradleFile = androidProject.appDir.resolve("build.gradle")
        buildGradleFile.appendText("""
            permissionCheck {
                reportDirectory.set(layout.buildDirectory.dir("reports"))
            }
        """.trimIndent())

        androidProject.setupBaseline()
        androidProject.runTask("checkDebugPermissions", gradleVersion = gradleVersion, agpVersion = agpVersion)

        val reportDirectory = androidProject.appDir.resolve("build").resolve("reports")
        assertTrue(reportDirectory.resolve("permission-check-report-debug.xml").exists())
        assertTrue(reportDirectory.resolve("permission-check-report-debug.html").exists())

        val oldReportDirectory = androidProject.appDir.resolve("build").resolve("reports").resolve("permissioncheck")
        assertFalse(oldReportDirectory.resolve("permission-check-report-debug.xml").exists())
        assertFalse(oldReportDirectory.resolve("permission-check-report-debug.html").exists())
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Task fails if the specified baseline is not an XML file`(gradleVersion: String, agpVersion: String) {
        val buildGradleFile = androidProject.appDir.resolve("build.gradle")
        buildGradleFile.appendText("""
            permissionCheck {
                baselineFile.set(layout.projectDirectory.file("invalid-baseline.json"))
            }
        """.trimIndent())

        val buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion,
            expectFailure = true
        )
        assertTrue(buildResult.output.contains("The permission baseline has to be a .xml file"))
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Up-to-date checks work correctly when manifest changes`(gradleVersion: String, agpVersion: String) {
        androidProject.setupBaseline()

        var buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertEquals(TaskOutcome.SUCCESS, buildResult.tasks.outcomeOf("checkDebugPermissions"))

        buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.tasks.outcomeOf("checkDebugPermissions"))

        val manifestFile = androidProject.appDir.resolve("src").resolve("main").resolve("AndroidManifest.xml")
        manifestFile.writeText(manifestFile.readText().replace("simonschiller", "johndoe")) // Trigger change

        buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertEquals(TaskOutcome.SUCCESS, buildResult.tasks.outcomeOf("checkDebugPermissions"))

        buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.tasks.outcomeOf("checkDebugPermissions"))
    }

    @ParameterizedTest
    @ArgumentsSource(TestVersions::class)
    fun `Up-to-date checks work correctly when baseline changes`(gradleVersion: String, agpVersion: String) {
        androidProject.setupBaseline()

        var buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertEquals(TaskOutcome.SUCCESS, buildResult.tasks.outcomeOf("checkDebugPermissions"))

        buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.tasks.outcomeOf("checkDebugPermissions"))

        androidProject.baselineFile.appendText(" ") // Trigger change

        buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertEquals(TaskOutcome.SUCCESS, buildResult.tasks.outcomeOf("checkDebugPermissions"))

        buildResult = androidProject.runTask(
            "checkDebugPermissions",
            gradleVersion = gradleVersion,
            agpVersion = agpVersion
        )
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.tasks.outcomeOf("checkDebugPermissions"))
    }
}
