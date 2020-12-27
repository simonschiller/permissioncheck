package io.github.simonschiller.permissioncheck.testutil

import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File
import java.nio.file.Files
import java.util.*

class AndroidProjectExtension : BeforeEachCallback, AfterEachCallback {
    lateinit var rootDir: File
        private set

    val appDir: File get() = rootDir.resolve("app")
    val baselineFile: File get() = appDir.resolve("permission-baseline.xml")

    fun runTask(vararg arguments: String, gradleVersion: String, agpVersion: String, expectFailure: Boolean = false): BuildResult {
        val runner = GradleRunner.create()
            .withProjectDir(rootDir)
            .withGradleVersion(gradleVersion)
            .withArguments(*arguments, "--stacktrace")

        // Switch to specified AGP version
        val buildGradle = rootDir.resolve("build.gradle")
        buildGradle.writeText(buildGradle.readText().replace("<AGP_VERSION>", agpVersion))

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
        createRootBuildGradle()
        createAppBuildGradle()
        createAndroidManifest()
    }

    override fun afterEach(context: ExtensionContext) {
        rootDir.deleteRecursively()
    }

    private fun createSettingsGradle() {
        val settingsGradle = rootDir.resolve("settings.gradle")
        settingsGradle.writeText("include(':app')")
    }

    private fun createLocalProperties() {
        val localProperties = rootDir.resolve("local.properties")
        val androidHome = getAndroidHome()
        localProperties.writeText("sdk.dir=$androidHome")
    }

    private fun createRootBuildGradle() {
        val buildGradle = rootDir.resolve("build.gradle")
        buildGradle.writeText("""
            buildscript {
                repositories {
		            google()
		            mavenCentral()
                    mavenLocal()
		            jcenter()
	            }

	            dependencies {
		            classpath("com.android.tools.build:gradle:<AGP_VERSION>")
                    classpath("io.github.simonschiller:plugin:+")
	            }
            }
            
        """.trimIndent())
    }

    private fun createAppBuildGradle() {
        appDir.mkdirs()

        val buildGradle = appDir.resolve("build.gradle")
        buildGradle.writeText("""
            apply plugin: "com.android.application"
            apply plugin: "io.github.simonschiller.permissioncheck"
            
            repositories {
                google()
                mavenCentral()
                jcenter()
            }

            android {
            	compileSdkVersion(30)

        	    defaultConfig {
            		minSdkVersion(21)
            		targetSdkVersion(30)
            	}
            
                lintOptions {
                    check("")
                }
            }
            
        """.trimIndent())
    }

    private fun createAndroidManifest() {
        val mainDir = appDir.resolve("src").resolve("main")
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

fun List<BuildTask>.outcomeOf(taskName: String): TaskOutcome {
    val task = singleOrNull { it.path == ":app:$taskName" } ?: error("Could not find task with name $taskName")
    return task.outcome
}
