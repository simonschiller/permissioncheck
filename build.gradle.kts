import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.embeddedKotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
	repositories {
		google()
		mavenCentral()
		mavenLocal()
		jcenter()
	}
	dependencies {
		classpath("com.android.tools.build:gradle:4.2.0-beta03")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$embeddedKotlinVersion")

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

subprojects {
	repositories {
		google()
		mavenCentral()
		jcenter()
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
