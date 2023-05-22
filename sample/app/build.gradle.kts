plugins {
	id("com.android.application")
	id("com.telefonica.manifestcheck")
}

android {
	compileSdkVersion(33)

	defaultConfig {
		minSdkVersion(23)
		targetSdkVersion(33)
	}

	lintOptions {
		checkOnly("") // Disable all Lint checks
	}
}

dependencies {
	implementation(project(":sample:library"))
}

permissionCheck {
	baselineFile.set(layout.projectDirectory.file("sample-baseline.xml"))
	reportDirectory.set(layout.buildDirectory.dir("reports"))
	strict.set(true)
}
