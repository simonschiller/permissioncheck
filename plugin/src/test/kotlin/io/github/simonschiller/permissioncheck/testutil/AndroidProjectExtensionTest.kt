package io.github.simonschiller.permissioncheck.testutil

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AndroidProjectExtensionTest {

    @JvmField
    @RegisterExtension
    val androidProject = AndroidProjectExtension()

    @Test
    fun `AndroidProjectExtension creates project that works correctly`() {
        androidProject.runTask(
            "tasks",
            "--all",
            gradleVersion = TestVersions.LATEST_GRADLE_VERSION,
            agpVersion = TestVersions.LATEST_AGP_VERSION
        )
    }
}
