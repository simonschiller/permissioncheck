package io.github.simonschiller.permissioncheck.config

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.extension.AndroidComponentsExtension
import io.github.simonschiller.permissioncheck.PermissionCheckExtension
import org.gradle.api.Project

/** Configures the tasks for AGP versions 4.2.0-alpha01 and above. */
@Suppress("UnstableApiUsage")
class TaskConfiguratorV3 : TaskConfigurator() {

    override fun configureTasks(project: Project, extension: PermissionCheckExtension) {
        val compositeTask = registerCompositeTask(project, extension)

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val variantTask = registerVariantTask(project, extension, variant.name)
            variantTask.configure { task ->
                task.mergedManifests.add(variant.artifacts.get(ArtifactType.MERGED_MANIFEST))
            }

            compositeTask.configure { task ->
                task.variants.add(variant.name)
                task.mergedManifests.add(variant.artifacts.get(ArtifactType.MERGED_MANIFEST))
            }
        }
    }
}
