package io.github.simonschiller.permissioncheck.config

import com.android.build.VariantOutput
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.ManifestProcessorTask
import io.github.simonschiller.permissioncheck.PermissionCheckExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

/** Configures the tasks for AGP versions 4.0.1 and below. */
@Suppress("UnstableApiUsage")
class TaskConfiguratorV1 : TaskConfigurator() {

    override fun configureTasks(project: Project, extension: PermissionCheckExtension) {
        val compositeTask = registerCompositeTask(project, extension)

        val appExtension = project.extensions.getByType(AppExtension::class.java)
        appExtension.applicationVariants.configureEach { variant ->
            val variantTask = registerVariantTask(project, extension, variant.name)
            variantTask.configure { task ->
                task.mergedManifests.add(getMergedManifestFile(project, variant))
                task.dependsOn(getManifestMergerTask(variant))
            }

            compositeTask.configure { task ->
                task.variants.add(variant.name)
                task.mergedManifests.add(getMergedManifestFile(project, variant))
                task.dependsOn(getManifestMergerTask(variant))
            }
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
