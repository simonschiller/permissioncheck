package io.github.simonschiller.permissioncheck

import com.android.build.VariantOutput
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.util.*

@Suppress("UnstableApiUsage")
class PermissionCheckPlugin : Plugin<Project> {
    private lateinit var extension: PermissionCheckExtension

    override fun apply(project: Project) {
        extension = project.extensions.create("permissionCheck", PermissionCheckExtension::class.java)

        // Register tasks once the Android app plugin is available
        project.plugins.configureEach {
            if (this !is AppPlugin) {
                return@configureEach // Only applicable to app modules
            }

            // Register composite task
            val appExtension = project.extensions.getByType(AppExtension::class.java)
            registerCompositeTask(project, appExtension.applicationVariants)

            // Register task for each variant
            appExtension.applicationVariants.configureEach {
                registerVariantTask(project, this)
            }
        }
    }

    private fun registerCompositeTask(project: Project, applicationVariants: DomainObjectSet<ApplicationVariant>) {
        val task = project.tasks.register("checkPermissions", PermissionCheckTask::class.java) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Checks permissions of all variants for regressions"

            applicationVariants.configureEach {
                variants.add(name)
                mergedManifests.add(getMergedManifestFile(this)) // Takes care of task dependencies automatically
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

    private fun registerVariantTask(project: Project, variant: ApplicationVariant) {
        val taskName = "check${variant.name.capitalize(Locale.ROOT)}Permissions"

        project.tasks.register(taskName, PermissionCheckTask::class.java) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Checks permissions of the ${variant.name} variant for regressions"

            variants.add(variant.name)
            mergedManifests.add(getMergedManifestFile(variant))
            baseline.set(extension.baselineFile)

            xmlReport.set(extension.reportDirectory.file("permission-check-report-${variant.name}.xml"))
            htmlReport.set(extension.reportDirectory.file("permission-check-report-${variant.name}.html"))

            recreate.set(false)
            strict.set(extension.strict)
        }
    }

    // Extracts the location of the merged manifest
    private fun getMergedManifestFile(variant: ApplicationVariant): Provider<RegularFile> {
        val output = variant.outputs.single { it.outputType == VariantOutput.MAIN }
        return output.processManifestProvider.flatMap { task ->
            task.manifestOutputDirectory.file("AndroidManifest.xml")
        }
    }
}
