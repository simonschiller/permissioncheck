plugins {
    id("com.android.library")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
    }

    lintOptions {
        checkOnly("") // Disable all Lint checks
    }
}
