package io.github.simonschiller.permissioncheck

import io.github.simonschiller.permissioncheck.internal.BaselineHandler
import io.github.simonschiller.permissioncheck.internal.ManifestParser
import io.github.simonschiller.permissioncheck.internal.PermissionChecker
import io.github.simonschiller.permissioncheck.internal.report.HtmlReporter
import io.github.simonschiller.permissioncheck.internal.report.LogReporter
import io.github.simonschiller.permissioncheck.internal.report.XmlReporter
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

@CacheableTask
@Suppress("UnstableApiUsage")
open class PermissionCheckTask : DefaultTask() {

    @Input
    val variants: ListProperty<String> = project.objects.listProperty()

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    val mergedManifests: ListProperty<RegularFile> = project.objects.listProperty()

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

    @OutputFile
    val xmlReport: RegularFileProperty = project.objects.fileProperty()

    @OutputFile
    val htmlReport: RegularFileProperty = project.objects.fileProperty()

    @Input
    @Option(option = "recreate", description = "Recreates the permissions baseline if specified")
    val recreate: Property<Boolean> = project.objects.property()

    @Input
    @Option(option = "strict", description = "Perform strict checking (also detect permission removals, ...)")
    val strict: Property<Boolean> = project.objects.property()

    @TaskAction
    fun checkAppPermissions() {
        val manifestParser = ManifestParser()
        val manifestPermissions = mergedManifests.get().map { manifest ->
            manifestParser.parsePermissions(manifest.asFile)
        }
        val permissions = variants.get().zip(manifestPermissions).toMap()

        val baselineFile = baseline.get().asFile
        val baselineHandler = BaselineHandler(baselineFile)

        // Create (or recreate) the baseline if needed
        if (recreate.get() || !baselineFile.exists()) {
            baselineHandler.serialize(permissions)
            project.logger.lifecycle("Created baseline at $baselineFile")
            if (!recreate.get()) { // New baseline created without explicit flag -> fail the build
                abortBuild()
            }
            return // No need to verify if the baseline has just been created
        }

        // Parse the existing baseline
        val baselinePermissions = baselineHandler.deserialize()
        if (!permissions.all { baselinePermissions.containsKey(it.key) }) {
            baselineHandler.serialize(permissions) // Update baseline, because one or more variants are missing
            project.logger.lifecycle("Created baseline at $baselineFile")
            abortBuild()
        }

        // Make sure the current permissions match the ones from the baseline
        val permissionChecker = PermissionChecker()
        val violations = permissions.mapValues { (variantName, variantPermissions) ->
            permissionChecker.findViolations(baselinePermissions.getValue(variantName), variantPermissions, strict.get())
        }

        // Generate reports based on the violations
        val reporters = listOf(
            LogReporter(project.logger),
            XmlReporter(xmlReport.get().asFile),
            HtmlReporter(htmlReport.get().asFile)
        )
        reporters.forEach { reporter -> reporter.report(violations) }

        // Fail the task if any violations have been found
        val violationCount = violations.values.fold(0) { sum, variantViolations -> sum + variantViolations.size }
        if (violationCount != 0) {
            throw GradleException("Found $violationCount violation(s) while checking permissions")
        }
    }

    // Fails the task so that baselines are not created accidentally
    private fun abortBuild(): Nothing {
        project.logger.lifecycle("Breaking build so that baselines are not created on accident, a rerun should succeed")
        project.logger.lifecycle("Use the --recreate flag to recreate baselines without failing the build")
        throw GradleException("Aborting build since new baseline was created")
    }
}
