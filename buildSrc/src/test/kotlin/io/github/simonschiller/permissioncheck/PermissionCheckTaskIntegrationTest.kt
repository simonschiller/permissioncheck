package io.github.simonschiller.permissioncheck

import io.github.simonschiller.permissioncheck.testutil.AndroidProjectExtension
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.File

class PermissionCheckTaskIntegrationTest {

    @JvmField
    @RegisterExtension
    val androidProject = AndroidProjectExtension()

    private val baselineFile: File get() = androidProject.rootDir.resolve("permission-baseline.xml")

    @Test
    fun `Baseline file is created if it does not exist yet`() {
        val buildResult = GradleRunner.create()
            .withProjectDir(androidProject.rootDir)
            .withPluginClasspath()
            .withArguments("checkDebugPermissions")
            .buildAndFail()

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

        assertEquals(baseline, baselineFile.readText().replace("\r", ""))
    }

    @Test
    fun `Baseline file is extended for new variants`() {
        baselineFile.writeText("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="release">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
            </baseline>
        """.trimIndent())

        val buildResult = GradleRunner.create()
            .withProjectDir(androidProject.rootDir)
            .withPluginClasspath()
            .withArguments("checkDebugPermissions")
            .buildAndFail()

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

        assertEquals(baseline, baselineFile.readText().replace("\r", ""))
    }

    @Test
    fun `Task completes without failure when recreating baselines`() {
        val buildResult = GradleRunner.create()
            .withProjectDir(androidProject.rootDir)
            .withPluginClasspath()
            .withArguments("checkDebugPermissions", "--recreate")
            .build()

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

        assertEquals(baseline, baselineFile.readText().replace("\r", ""))
    }

    @Test
    fun `Task succeeds silently if there are no violations`() {
        baselineFile.writeText("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
            </baseline>
        """.trimIndent())

        val buildResult = GradleRunner.create()
            .withProjectDir(androidProject.rootDir)
            .withPluginClasspath()
            .withArguments("checkDebugPermissions")
            .build()

        assertTrue(buildResult.output.contains("Found no violations"))
    }

    @Test
    fun `Task fails if there are violations`() {
        baselineFile.writeText("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission maxSdkVersion="24" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
            </baseline>
        """.trimIndent())

        val buildResult = GradleRunner.create()
            .withProjectDir(androidProject.rootDir)
            .withPluginClasspath()
            .withArguments("checkDebugPermissions")
            .buildAndFail()

        assertTrue(buildResult.output.contains("Found 2 violation(s)"))
    }

    @Test
    fun `Task fails if there are strict mode violations and strict mode is enabled`() {
        baselineFile.writeText("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission name="android.permission.ACCESS_FINE_LOCATION"/>
                    <uses-permission maxSdkVersion="24" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
            </baseline>
        """.trimIndent())

        val buildResult = GradleRunner.create()
            .withProjectDir(androidProject.rootDir)
            .withPluginClasspath()
            .withArguments("checkDebugPermissions", "--strict")
            .buildAndFail()

        assertTrue(buildResult.output.contains("Found 2 violation(s)"))
    }

    @Test
    fun `All variants are checked as part of the default check task`() {
        baselineFile.writeText("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
                <variant name="release">
                    <uses-permission name="android.permission.INTERNET"/>
                    <uses-permission maxSdkVersion="26" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                </variant>
            </baseline>
        """.trimIndent())

        val buildResult = GradleRunner.create()
            .withProjectDir(androidProject.rootDir)
            .withPluginClasspath()
            .withArguments("check")
            .build()

        assertTrue(buildResult.tasks.any { it.path == ":checkDebugPermissions" })
        assertTrue(buildResult.tasks.any { it.path == ":checkReleasePermissions" })
    }
}
