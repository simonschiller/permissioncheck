plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly("com.android.tools.build:gradle:4.2.0-beta03")

    api(project(":plugin-core"))
}
