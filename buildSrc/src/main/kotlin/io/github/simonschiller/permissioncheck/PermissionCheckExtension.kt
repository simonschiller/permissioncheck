package io.github.simonschiller.permissioncheck

import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class PermissionCheckExtension(objects: ObjectFactory, layout: ProjectLayout) {

    /** Location of the baseline file, should be somewhere in your project directory. */
    val baselineFile: Property<RegularFile> = objects.fileProperty()

    /** When enabled, removed permissions and decreased max SDK versions will also be detected. */
    val strict: Property<Boolean> = objects.property()

    // Setup default values
    init {
        baselineFile.convention(layout.projectDirectory.file("permission-baseline.xml"))
        strict.convention(false)
    }
}
