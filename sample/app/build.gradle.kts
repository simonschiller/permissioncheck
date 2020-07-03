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

permissionCheck {
	baselineFile.set(layout.projectDirectory.file("sample-baseline.xml"))
	reportDirectory.set(layout.buildDirectory.dir("reports"))
	strict.set(true)
}
