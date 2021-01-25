package io.github.simonschiller.permissioncheck.config

import io.github.simonschiller.permissioncheck.PermissionCheckExtension
import io.github.simonschiller.permissioncheck.PermissionCheckTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.util.*

/**
 * Base class to configure the Gradle tasks, different implementations are used to stay compatible with older AGP
 * versions.
 */
abstract class TaskConfigurator {
    abstract fun configureTasks(project: Project, extension: PermissionCheckExtension)

    protected fun registerCompositeTask(
        project: Project,
        extension: PermissionCheckExtension
    ): TaskProvider<PermissionCheckTask> {
        val task = project.tasks.register("checkPermissions", PermissionCheckTask::class.java) { task ->
            task.group = LifecycleBasePlugin.VERIFICATION_GROUP
            task.description = "Checks permissions of all variants for regressions"

            task.baseline.set(extension.baselineFile)

            task.xmlReport.set(extension.reportDirectory.file("permission-check-report.xml"))
            task.htmlReport.set(extension.reportDirectory.file("permission-check-report.html"))

            task.recreate.set(false)
            task.strict.set(extension.strict)
        }

        // Execute composite task as part of the standard Gradle check task
        project.tasks.named("check").configure { check ->
            check.dependsOn(task)
        }
        return task
    }

    protected fun registerVariantTask(
        project: Project,
        extension: PermissionCheckExtension,
        variantName: String
    ): TaskProvider<PermissionCheckTask> {
        val taskName = "check${variantName.capitalize(Locale.ROOT)}Permissions"

        return project.tasks.register(taskName, PermissionCheckTask::class.java) { task ->
            task.group = LifecycleBasePlugin.VERIFICATION_GROUP
            task.description = "Checks permissions of the $variantName variant for regressions"

            task.variants.add(variantName)
            task.baseline.set(extension.baselineFile)

            task.xmlReport.set(extension.reportDirectory.file("permission-check-report-$variantName.xml"))
            task.htmlReport.set(extension.reportDirectory.file("permission-check-report-$variantName.html"))

            task.recreate.set(false)
            task.strict.set(extension.strict)
        }
    }
}
