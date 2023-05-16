plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly("com.android.tools.build:gradle:7.0.0")

    api(project(":plugin-core"))
}
