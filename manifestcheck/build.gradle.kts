plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "com.telefonica"
version = "1.0.1" // Also update the version in the README

val uber: Configuration by configurations.creating

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly("com.android.tools.build:gradle:7.0.0")

    uber(project(":plugin-configurator-v1"))
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
        create("manifestcheck") {
            id = "com.telefonica.manifestcheck"
            displayName = "ManifestCheck"
            description = "ManifestCheck is a Gradle plugin that helps you catch Android permission/feature regressions automatically."
            implementationClass = "io.github.simonschiller.permissioncheck.PermissionCheckPlugin"
        }
    }
}
pluginBundle {
    website = "https://github.com/Telefonica/android-permissioncheck"
    vcsUrl = "https://github.com/Telefonica/android-permissioncheck"
    tags = listOf("manifestcheck", "permissions")
}
