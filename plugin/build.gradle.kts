import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "io.github.simonschiller"
version = "1.5.0" // Also update the version in the README

repositories {
    google()
    mavenCentral()
    jcenter()
}

dependencies {
    compileOnly("com.android.tools.build:gradle:4.1.1")

    val junitVersion = "5.7.0"
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.ExperimentalStdlibApi"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    dependsOn("publishToMavenLocal")

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

gradlePlugin {
    plugins {
        create("permissioncheck") {
            id = "io.github.simonschiller.permissioncheck"
            implementationClass = "io.github.simonschiller.permissioncheck.PermissionCheckPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/simonschiller/permissioncheck"
    vcsUrl = "https://github.com/simonschiller/permissioncheck"
    description = "PermissionCheck is a Gradle plugin that helps you catch Android permission regressions automatically."
    tags = listOf("android", "permissions")

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = "permissioncheck"
    }

    (plugins) {
        "permissioncheck" {
            displayName = "PermissionCheck"
        }
    }
}
