plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly("com.android.tools.build:gradle:4.1.2")

    api(project(":plugin-core"))
}
