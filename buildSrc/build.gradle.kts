import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "io.github.simonschiller"
version = "1.0.0" // Also update the version in the README

repositories {
    google()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("com.android.tools.build:gradle:4.0.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.ExperimentalStdlibApi"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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
