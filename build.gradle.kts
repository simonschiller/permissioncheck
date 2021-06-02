import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.embeddedKotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
	repositories {
		google()
		mavenCentral()
		mavenLocal()
	}
	dependencies {
		classpath("com.android.tools.build:gradle:7.0.0")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$embeddedKotlinVersion")

//		classpath("io.github.simonschiller:plugin:+") // Uncomment to use the sample
	}
}

allprojects {
	repositories {
		google()
		mavenCentral()
	}
}

subprojects {
	repositories {
		google()
		mavenCentral()
	}

	tasks.withType<KotlinCompile>().configureEach {
		kotlinOptions {
			jvmTarget = "1.8"
			freeCompilerArgs = listOf("-Xopt-in=kotlin.ExperimentalStdlibApi")
		}
	}

	tasks.withType<Test>().configureEach {
		useJUnitPlatform()

		testLogging {
			events("passed", "skipped", "failed")
			exceptionFormat = TestExceptionFormat.FULL
		}
	}
}
