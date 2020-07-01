package io.github.simonschiller.permissioncheck

import com.android.build.VariantOutput
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.util.*

@Suppress("UnstableApiUsage")
class PermissionCheckPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("permissionCheck", PermissionCheckExtension::class.java)

        // Register tasks once the Android app plugin is available
        project.plugins.configureEach {
            if (this !is AppPlugin) {
                return@configureEach // Only applicable to app modules
            }

            val appExtension = project.extensions.getByType(AppExtension::class.java)
            appExtension.applicationVariants.configureEach {
                registerTask(project, this, extension)
            }
        }
    }

    private fun registerTask(project: Project, variant: ApplicationVariant, extension: PermissionCheckExtension) {
        val taskName = "check${variant.name.capitalize(Locale.ROOT)}Permissions"

        val task = project.tasks.register(taskName, PermissionCheckTask::class.java) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Checks ${variant.name} permissions for regressions"

            variantName.set(variant.name)
            mergedManifest.set(getMergedManifestFile(variant)) // Takes care of task dependencies automatically
            baseline.set(extension.baselineFile)
            recreate.set(false)
            strict.set(extension.strict)
        }

        // Execute the task as part of the standard Gradle check task
        project.tasks.named("check").configure {
            dependsOn(task)
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
