package io.github.simonschiller.permissioncheck.config

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import io.github.simonschiller.permissioncheck.PermissionCheckExtension
import org.gradle.api.Project

/** Configures the tasks for AGP versions 7.0.0-alpha01 and above. */
class TaskConfiguratorV4 : TaskConfigurator() {

    override fun configureTasks(project: Project, extension: PermissionCheckExtension) {
        val compositeTask = registerCompositeTask(project, extension)

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val variantTask = registerVariantTask(project, extension, variant.name)
            variantTask.configure { task ->
                task.mergedManifests.add(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            }

            compositeTask.configure { task ->
                task.variants.add(variant.name)
                task.mergedManifests.add(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            }
        }
    }
}
