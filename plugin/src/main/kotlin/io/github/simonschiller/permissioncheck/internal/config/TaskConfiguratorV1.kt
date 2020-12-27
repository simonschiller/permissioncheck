@file:Suppress("DEPRECATION") // Deprecated APIs are used for backwards compatibility

package io.github.simonschiller.permissioncheck.internal.config

import com.android.build.VariantOutput
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.ManifestProcessorTask
import io.github.simonschiller.permissioncheck.PermissionCheckExtension
import io.github.simonschiller.permissioncheck.PermissionCheckTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.util.*

/** Configures the tasks for AGP versions 4.0.1 and below. */
@Suppress("UnstableApiUsage")
internal class TaskConfiguratorV1 : TaskConfigurator {

    override fun configureTasks(project: Project, extension: PermissionCheckExtension) {

        // Register composite task
        val appExtension = project.extensions.getByType(AppExtension::class.java)
        registerCompositeTask(project, extension, appExtension.applicationVariants)

        // Register task for each variant
        appExtension.applicationVariants.configureEach {
            registerVariantTask(project, extension, this)
        }
    }

    private fun registerCompositeTask(
        project: Project,
        extension: PermissionCheckExtension,
        applicationVariants: DomainObjectSet<ApplicationVariant>
    ) {
        val task = project.tasks.register("checkPermissions", PermissionCheckTask::class.java) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Checks permissions of all variants for regressions"

            applicationVariants.configureEach {
                variants.add(name)
                mergedManifests.add(getMergedManifestFile(project, this))

                dependsOn(getManifestMergerTask(this))
            }
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
    }

    private fun registerVariantTask(
        project: Project,
        extension: PermissionCheckExtension,
        variant: ApplicationVariant
    ) {
        val taskName = "check${variant.name.capitalize(Locale.ROOT)}Permissions"

        project.tasks.register(taskName, PermissionCheckTask::class.java) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Checks permissions of the ${variant.name} variant for regressions"

            variants.add(variant.name)
            mergedManifests.add(getMergedManifestFile(project, variant))
            baseline.set(extension.baselineFile)

            xmlReport.set(extension.reportDirectory.file("permission-check-report-${variant.name}.xml"))
            htmlReport.set(extension.reportDirectory.file("permission-check-report-${variant.name}.html"))

            recreate.set(false)
            strict.set(extension.strict)

            dependsOn(getManifestMergerTask(variant))
        }
    }

    private fun getManifestMergerTask(variant: ApplicationVariant): TaskProvider<ManifestProcessorTask> {
        val output = variant.outputs.single { it.outputType == VariantOutput.MAIN }
        return output.processManifestProvider
    }

    private fun getMergedManifestFile(project: Project, variant: ApplicationVariant): Provider<RegularFile> {
        return project.layout.buildDirectory.map { buildDir ->
            buildDir.dir("intermediates").dir("merged_manifests").dir(variant.dirName).file("AndroidManifest.xml")
        }
    }
}
