package io.github.simonschiller.permissioncheck

import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class PermissionCheckExtension(objects: ObjectFactory, layout: ProjectLayout) {

    /** Location of the baseline file, should be somewhere in your project directory. */
    val baselineFile: RegularFileProperty = objects.fileProperty()

    /** Directory for all generated reports, should be somewhere in your build directory. */
    val reportDirectory: DirectoryProperty = objects.directoryProperty()

    /** When enabled, removed permissions and decreased max SDK versions will also be detected. */
    val strict: Property<Boolean> = objects.property()

    // Setup default values
    init {
        baselineFile.convention(layout.projectDirectory.file("permission-baseline.xml"))
        reportDirectory.convention(layout.buildDirectory.dir("reports/permissioncheck"))
        strict.convention(false)
    }
}
