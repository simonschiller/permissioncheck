package io.github.simonschiller.permissioncheck.internal.config

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationVariantProperties
import io.github.simonschiller.permissioncheck.PermissionCheckTask
import io.github.simonschiller.permissioncheck.PermissionCheckExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.util.*

/** Configures the tasks for AGP versions 4.1.0 and above. */
@Suppress("UnstableApiUsage")
internal class TaskConfiguratorV2 : TaskConfigurator {

    override fun configureTasks(project: Project, extension: PermissionCheckExtension) {

        // Register composite task
        val compositeTask = registerCompositeTask(project, extension)

        // Register tasks for each variant
        val applicationExtension = project.extensions.getByType(ApplicationExtension::class.java)
        applicationExtension.onVariantProperties {
            registerVariantTask(project, extension, this)

            compositeTask.configure {
                variants.add(this@onVariantProperties.name)
                mergedManifests.add(artifacts.get(ArtifactType.MERGED_MANIFEST))
            }
        }
    }

    private fun registerCompositeTask(
        project: Project,
        extension: PermissionCheckExtension
    ): TaskProvider<PermissionCheckTask> {
        val task = project.tasks.register("checkPermissions", PermissionCheckTask::class.java) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Checks permissions of all variants for regressions"

            baseline.set(extension.baselineFile)

            xmlReport.set(extension.reportDirectory.file("permission-check-report.xml"))
            htmlReport.set(extension.reportDirectory.file("permission-check-report.html"))

            recreate.set(false)
            strict.set(extension.strict)
        }

        // Execute this task as part of the standard Gradle check task
        project.tasks.named("check").configure {
            dependsOn(task)
        }
        return task
    }

    private fun registerVariantTask(
        project: Project,
        extension: PermissionCheckExtension,
        variant: ApplicationVariantProperties
    ) {
        val taskName = "check${variant.name.capitalize(Locale.ROOT)}Permissions"

        project.tasks.register(taskName, PermissionCheckTask::class.java) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Checks permissions of the ${variant.name} variant for regressions"

            variants.add(variant.name)
            mergedManifests.add(variant.artifacts.get(ArtifactType.MERGED_MANIFEST))
            baseline.set(extension.baselineFile)

            xmlReport.set(extension.reportDirectory.file("permission-check-report-${variant.name}.xml"))
            htmlReport.set(extension.reportDirectory.file("permission-check-report-${variant.name}.html"))

            recreate.set(false)
            strict.set(extension.strict)
        }
    }
}
