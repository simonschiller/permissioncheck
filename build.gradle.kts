buildscript {
	repositories {
		google()
		mavenCentral()
		jcenter()
	}
	dependencies {
		classpath("com.android.tools.build:gradle:4.0.0")
	}
}

allprojects {
	repositories {
		google()
		mavenCentral()
		jcenter()
	}
}

tasks.register("clean", Delete::class.java) {
	delete(rootProject.buildDir)
}
