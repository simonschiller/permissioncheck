package io.github.simonschiller.permissioncheck.internal.config

import io.github.simonschiller.permissioncheck.PermissionCheckExtension
import org.gradle.api.Project

internal interface TaskConfigurator {
    fun configureTasks(project: Project, extension: PermissionCheckExtension)
}
