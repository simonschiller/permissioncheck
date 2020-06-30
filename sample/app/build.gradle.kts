plugins {
	id("com.android.application")
	id("io.github.simonschiller.permissioncheck")
}

android {
	compileSdkVersion(29)

	defaultConfig {
		minSdkVersion(21)
		targetSdkVersion(29)
	}

	lintOptions {
		check("") // Disable all Lint checks
	}
}

dependencies {
	implementation(project(":sample:library"))
}
