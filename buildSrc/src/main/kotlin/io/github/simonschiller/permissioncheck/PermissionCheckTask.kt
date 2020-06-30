package io.github.simonschiller.permissioncheck

import io.github.simonschiller.permissioncheck.internal.BaselineHandler
import io.github.simonschiller.permissioncheck.internal.ManifestParser
import io.github.simonschiller.permissioncheck.internal.PermissionChecker
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@CacheableTask
@Suppress("UnstableApiUsage")
open class PermissionCheckTask : DefaultTask() {

    @Input
    val variantName: Property<String> = project.objects.property()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    val mergedManifest: RegularFileProperty = project.objects.fileProperty()

    @Internal // Exposed via the optionalBaseline property, so the task can run if the baseline does not exist yet
    val baseline: RegularFileProperty = project.objects.fileProperty()

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    val optionalBaseline: Provider<RegularFile> = baseline.flatMap { file ->
        if (file.asFile.exists()) {
            project.provider { file }
        } else {
            project.provider { null }
        }
    }

    @Input
    @Option(option = "recreate", description = "Recreates the permissions baseline if specified")
    val recreate: Property<Boolean> = project.objects.property()

    @Input
    @Option(option = "strict", description = "Perform strict checking (also detect permission removals, ...)")
    val strict: Property<Boolean> = project.objects.property()

    @TaskAction
    fun checkAppPermissions() {
        val manifestParser = ManifestParser()
        val manifestPermissions = manifestParser.parsePermissions(mergedManifest.get().asFile)

        val variant = variantName.get()
        val baselineFile = baseline.get().asFile
        val baselineHandler = BaselineHandler(baselineFile)

        // Create (or recreate) the baseline if needed
        if (recreate.get() || !baselineFile.exists()) {
            baselineHandler.serialize(variant, manifestPermissions)
            project.logger.lifecycle("Created baseline for variant $variant at $baselineFile")
            if (!recreate.get()) { // New baseline created without explicit flag -> fail the build
                abortBuild()
            }
            return // No need to verify if the baseline has just been created
        }

        // Parse the existing baseline
        val baselinePermissions = baselineHandler.deserialize()
        val variantPermissions = baselinePermissions[variant]
        if (variantPermissions == null) {
            baselineHandler.serialize(variant, manifestPermissions) // Create new baseline for variant
            project.logger.lifecycle("Created baseline for variant $variant at $baselineFile")
            abortBuild()
        }

        // Make sure the current permissions match the ones from the baseline
        val permissionChecker = PermissionChecker()
        val violations = permissionChecker.findViolations(variantPermissions, manifestPermissions, strict.get())
        if (violations.isEmpty()) {
            project.logger.lifecycle("Found no violations, all permissions match the baseline")
        } else {
            project.logger.error("Found ${violations.size} violation(s) while checking permissions:")
            violations.forEach { violation ->
                project.logger.error(violation.message)
            }
            throw GradleException("Found ${violations.size} violation(s) while checking permissions")
        }
    }

    // Fails the task so that baselines are not created accidentally
    private fun abortBuild(): Nothing {
        project.logger.lifecycle("Breaking build so that baselines are not created on accident, a rerun should succeed")
        project.logger.lifecycle("Use the --recreate flag to recreate baselines without failing the build")
        throw GradleException("Aborting build since new baseline was created")
    }
}
