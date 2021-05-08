plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.14.0"
}

group = "io.github.simonschiller"
version = "1.6.0" // Also update the version in the README

val uber: Configuration by configurations.creating

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly("com.android.tools.build:gradle:4.2.0")

    uber(project(":plugin-configurator-v1"))
    uber(project(":plugin-configurator-v2"))
    uber(project(":plugin-configurator-v3"))
    uber(project(":plugin-core"))

    testRuntimeOnly(Dependencies.JUNIT_5_ENGINE)
    testImplementation(Dependencies.JUNIT_5_API)
    testImplementation(Dependencies.JUNIT_5_PARAMS)
    testImplementation(gradleKotlinDsl())
}

configurations {
    compileClasspath.configure { extendsFrom(uber) }
}

// Publish all modules as part of a single uber plugin JAR
tasks.withType<Jar>().configureEach {
    from(uber.asSequence().filter { it.startsWith(rootDir) }.map { zipTree(it) }.asIterable())
}

tasks.withType<Test>().configureEach {
    dependsOn("publishToMavenLocal")
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
