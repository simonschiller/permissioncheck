package io.github.simonschiller.permissioncheck.config

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.dsl.ApplicationExtension
import io.github.simonschiller.permissioncheck.PermissionCheckExtension
import org.gradle.api.Project

/** Configures the tasks for AGP versions between 4.1.0 (inclusive) and 4.2.0-alpha01 (exclusive). */
@Suppress("UnstableApiUsage")
class TaskConfiguratorV2 : TaskConfigurator() {

    override fun configureTasks(project: Project, extension: PermissionCheckExtension) {
        val compositeTask = registerCompositeTask(project, extension)

        val applicationExtension = project.extensions.getByType(ApplicationExtension::class.java)
        applicationExtension.onVariantProperties { variant ->
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
