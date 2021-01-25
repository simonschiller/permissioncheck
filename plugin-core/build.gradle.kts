plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(gradleKotlinDsl())

    testRuntimeOnly(Dependencies.JUNIT_5_ENGINE)
    testImplementation(Dependencies.JUNIT_5_API)
    testImplementation(gradleKotlinDsl())
}
