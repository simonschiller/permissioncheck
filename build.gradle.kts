buildscript {
	repositories {
		google()
		mavenCentral()
		mavenLocal()
		jcenter()
	}
	dependencies {
		classpath("com.android.tools.build:gradle:4.1.1")
//		classpath("io.github.simonschiller:plugin:+") // Uncomment to use the sample
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
