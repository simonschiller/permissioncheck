package io.github.simonschiller.permissioncheck.testutil

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File
import java.nio.file.Files
import java.util.*

class AndroidProjectExtension : BeforeEachCallback, AfterEachCallback {
    lateinit var rootDir: File
        private set

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
        System.getenv("ANDROID_HOME")?.let { return it.replace("\\", "/") }

        val localProperties = File(System.getProperty("user.dir")).resolveSibling("local.properties")
        if (localProperties.exists()) {
            val properties = Properties()
            localProperties.inputStream().use { properties.load(it) }
            properties.getProperty("sdk.dir")?.let { return it.replace("\\", "/") }
        }
        error("Missing 'ANDROID_HOME' environment variable or local.properties with 'sdk.dir'")
    }
}
