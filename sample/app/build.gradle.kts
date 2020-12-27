plugins {
	id("com.android.application")
	id("io.github.simonschiller.permissioncheck")
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

dependencies {
	implementation(project(":sample:library"))
}

permissionCheck {
	baselineFile.set(layout.projectDirectory.file("sample-baseline.xml"))
	reportDirectory.set(layout.buildDirectory.dir("reports"))
	strict.set(true)
}
