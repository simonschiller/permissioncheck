package io.github.simonschiller.permissioncheck.testutil

import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File
import java.nio.file.Files
import java.util.*

class AndroidProjectExtension : BeforeEachCallback, AfterEachCallback {
    lateinit var rootDir: File
        private set

    val baselineFile: File get() = rootDir.resolve("permission-baseline.xml")

    fun runTask(vararg arguments: String, expectFailure: Boolean = false): BuildResult {
        val runner = GradleRunner.create()
            .withProjectDir(rootDir)
            .withPluginClasspath()
            .withArguments(*arguments)

        return if (expectFailure) {
            runner.buildAndFail()
        } else {
            runner.build()
        }
    }

    fun setupBaseline() {
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
    }

    fun setupBaselineWithViolations() {
        baselineFile.writeText("""
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <baseline>
                <variant name="debug">
                    <uses-permission maxSdkVersion="24" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                    <uses-permission name="android.permission.ACCESS_COARSE_LOCATION"/>
                </variant>
                <variant name="release">
                    <uses-permission maxSdkVersion="24" name="android.permission.CAMERA"/>
                    <uses-permission-sdk-23 name="android.permission.ACCESS_NETWORK_STATE"/>
                    <uses-permission name="android.permission.ACCESS_COARSE_LOCATION"/>
                </variant>
            </baseline>
        """.trimIndent())
    }

    override fun beforeEach(context: ExtensionContext) {
        rootDir = Files.createTempDirectory("permissioncheck-test").toFile()

        createSettingsGradle()
        createLocalProperties()
        createBuildGradle()
        createAndroidManifest()
    }

    override fun afterEach(context: ExtensionContext) {
        rootDir.deleteRecursively()
    }

    private fun createSettingsGradle() {
        val settingsGradle = rootDir.resolve("settings.gradle")
        settingsGradle.createNewFile()
    }

    private fun createLocalProperties() {
        val localProperties = rootDir.resolve("local.properties")
        val androidHome = getAndroidHome()
        localProperties.writeText("sdk.dir=$androidHome")
    }

    private fun createBuildGradle() {
        val buildGradle = rootDir.resolve("build.gradle")
        buildGradle.writeText("""
            plugins {
            	id("com.android.application")
            	id("io.github.simonschiller.permissioncheck")
            }
            
            repositories {
                google()
                mavenCentral()
                jcenter()
            }

            android {
            	compileSdkVersion(29)

        	    defaultConfig {
            		minSdkVersion(21)
            		targetSdkVersion(29)
            	}
            
                lintOptions {
                    check("")
                }
            }
            
        """.trimIndent())
    }

    private fun createAndroidManifest() {
        val mainDir = rootDir.resolve("src").resolve("main")
        mainDir.mkdirs()

        val androidManifest = mainDir.resolve("AndroidManifest.xml")
        androidManifest.writeText("""
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="io.github.simonschiller.permissioncheck.sample.app">

                <uses-permission android:name="android.permission.INTERNET" />
                <uses-permission android:name="android.permission.CAMERA" android:maxSdkVersion="26" />
                <uses-permission-sdk-23 android:name="android.permission.ACCESS_NETWORK_STATE" />

                <application />
            </manifest>
        """.trimIndent())
    }

    private fun getAndroidHome(): String {
        System.getenv("ANDROID_HOME")?.let { return it.normaliseLineSeparators() }

        val localProperties = File(System.getProperty("user.dir")).resolveSibling("local.properties")
        if (localProperties.exists()) {
            val properties = Properties()
            localProperties.inputStream().use { properties.load(it) }
            properties.getProperty("sdk.dir")?.let { return it.normaliseLineSeparators() }
        }
        error("Missing 'ANDROID_HOME' environment variable or local.properties with 'sdk.dir'")
    }
}
