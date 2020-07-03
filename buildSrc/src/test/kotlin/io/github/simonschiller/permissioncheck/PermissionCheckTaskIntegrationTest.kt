package io.github.simonschiller.permissioncheck

import io.github.simonschiller.permissioncheck.testutil.AndroidProjectExtension
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.File

class PermissionCheckTaskIntegrationTest {

    @JvmField
    @RegisterExtension
    val androidProject = AndroidProjectExtension()

    @Test
    fun `Baseline file is created if it does not exist yet`() {
        val buildResult = androidProject.runTask("checkDebugPermissions", expectFailure = true)
        assertTrue(buildResult.output.contains("Created baseline for variant debug"))
        assertTrue(buildResult.output.contains("Aborting build since new baseline was created"))

        val baseline = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(baseline, androidProject.baselineFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Baseline file is extended for new variants`() {
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

        val buildResult = androidProject.runTask("checkDebugPermissions", expectFailure = true)
        assertTrue(buildResult.output.contains("Created baseline for variant debug"))
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
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(baseline, androidProject.baselineFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Task completes without failure when recreating baselines`() {
        val buildResult = androidProject.runTask("checkDebugPermissions", "--recreate")
        assertTrue(buildResult.output.contains("Created baseline for variant debug"))

        val baseline = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
            </baseline>
            
        """.trimIndent()
        assertEquals(baseline, androidProject.baselineFile.readText().normaliseLineSeparators())
    }

    @Test
    fun `Task succeeds silently if there are no violations`() {
        androidProject.setupBaseline()
        val buildResult = androidProject.runTask("checkDebugPermissions")
        assertTrue(buildResult.output.contains("Found no violations"))
    }

    @Test
    fun `Task fails if there are violations`() {
        androidProject.setupBaselineWithViolations()
        val buildResult = androidProject.runTask("checkDebugPermissions", expectFailure = true)
        assertTrue(buildResult.output.contains("Found 2 violation(s)"))
    }

    @Test
    fun `Task fails if there are strict mode violations and strict mode is enabled via command line`() {
        androidProject.setupBaselineWithViolations()
        val buildResult = androidProject.runTask("checkDebugPermissions", "--strict", expectFailure = true)
        assertTrue(buildResult.output.contains("Found 3 violation(s)"))
    }

    @Test
    fun `Task fails if there are strict mode violations and strict mode is enabled via configuration`() {
        val buildGradleFile = androidProject.rootDir.resolve("build.gradle")
        buildGradleFile.appendText("""
            permissionCheck {
                strict.set(true)
            }
        """.trimIndent())

        androidProject.setupBaselineWithViolations()
        val buildResult = androidProject.runTask("checkDebugPermissions", expectFailure = true)
        assertTrue(buildResult.output.contains("Found 3 violation(s)"))
    }

    @Test
    fun `All variants are checked as part of the default check task`() {
        androidProject.setupBaseline()

        val buildResult = androidProject.runTask("check")
        assertTrue(buildResult.tasks.any { it.path == ":checkDebugPermissions" })
        assertTrue(buildResult.tasks.any { it.path == ":checkReleasePermissions" })
    }

    @Test
    fun `Configuring a different baseline location works correctly`() {
        val buildGradleFile = androidProject.rootDir.resolve("build.gradle")
        buildGradleFile.appendText("""
            permissionCheck {
                baselineFile.set(layout.projectDirectory.file("baselines/test-baseline.xml"))
            }
        """.trimIndent())

        val buildResult = androidProject.runTask("checkDebugPermissions", expectFailure = true)

        val baselineFile = androidProject.rootDir.resolve("baselines").resolve("test-baseline.xml")
        assertTrue(buildResult.output.contains(baselineFile.path))
        assertTrue(baselineFile.exists())
        assertFalse(androidProject.rootDir.resolve("permission-baseline.xml").exists())
    }

    @Test
    fun `Reports are generated when task is executed`() {
        androidProject.setupBaseline()
        androidProject.runTask("checkDebugPermissions")

        val reportDirectory = androidProject.rootDir.resolve("build").resolve("reports").resolve("permissioncheck")
        assertTrue(reportDirectory.resolve("permission-check-report-debug.xml").exists())
        assertTrue(reportDirectory.resolve("permission-check-report-debug.html").exists())
    }

    @Test
    fun `Configuring a different report location works correctly`() {
        val buildGradleFile = androidProject.rootDir.resolve("build.gradle")
        buildGradleFile.appendText("""
            permissionCheck {
                reportDirectory.set(layout.buildDirectory.dir("reports"))
            }
        """.trimIndent())

        androidProject.setupBaseline()
        androidProject.runTask("checkDebugPermissions")

        val reportDirectory = androidProject.rootDir.resolve("build").resolve("reports")
        assertTrue(reportDirectory.resolve("permission-check-report-debug.xml").exists())
        assertTrue(reportDirectory.resolve("permission-check-report-debug.html").exists())

        val oldReportDirectory = androidProject.rootDir.resolve("build").resolve("reports").resolve("permissioncheck")
        assertFalse(oldReportDirectory.resolve("permission-check-report-debug.xml").exists())
        assertFalse(oldReportDirectory.resolve("permission-check-report-debug.html").exists())
    }

    @Test
    fun `Task fails if the specified baseline is not an XML file`() {
        val buildGradleFile = androidProject.rootDir.resolve("build.gradle")
        buildGradleFile.appendText("""
            permissionCheck {
                baselineFile.set(layout.projectDirectory.file("invalid-baseline.json"))
            }
        """.trimIndent())

        val buildResult = androidProject.runTask("checkDebugPermissions", expectFailure = true)
        assertTrue(buildResult.output.contains("The permission baseline has to be a .xml file"))
    }

    @Test
    fun `Up-to-date checks work correctly when manifest changes`() {
        androidProject.setupBaseline()

        var buildResult = androidProject.runTask("checkDebugPermissions")
        assertEquals(TaskOutcome.SUCCESS, buildResult.tasks.single { it.path == ":checkDebugPermissions" }.outcome)

        buildResult = androidProject.runTask("checkDebugPermissions")
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.tasks.single { it.path == ":checkDebugPermissions" }.outcome)

        val manifestFile = androidProject.rootDir.resolve("src").resolve("main").resolve("AndroidManifest.xml")
        manifestFile.writeText(manifestFile.readText().replace("simonschiller", "johndoe")) // Trigger change

        buildResult = androidProject.runTask("checkDebugPermissions")
        assertEquals(TaskOutcome.SUCCESS, buildResult.tasks.single { it.path == ":checkDebugPermissions" }.outcome)

        buildResult = androidProject.runTask("checkDebugPermissions")
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.tasks.single { it.path == ":checkDebugPermissions" }.outcome)
    }

    @Test
    fun `Up-to-date checks work correctly when baseline changes`() {
        androidProject.setupBaseline()

        var buildResult = androidProject.runTask("checkDebugPermissions")
        assertEquals(TaskOutcome.SUCCESS, buildResult.tasks.single { it.path == ":checkDebugPermissions" }.outcome)

        buildResult = androidProject.runTask("checkDebugPermissions")
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.tasks.single { it.path == ":checkDebugPermissions" }.outcome)

        androidProject.baselineFile.appendText(" ") // Trigger change

        buildResult = androidProject.runTask("checkDebugPermissions")
        assertEquals(TaskOutcome.SUCCESS, buildResult.tasks.single { it.path == ":checkDebugPermissions" }.outcome)

        buildResult = androidProject.runTask("checkDebugPermissions")
        assertEquals(TaskOutcome.UP_TO_DATE, buildResult.tasks.single { it.path == ":checkDebugPermissions" }.outcome)
    }
}
