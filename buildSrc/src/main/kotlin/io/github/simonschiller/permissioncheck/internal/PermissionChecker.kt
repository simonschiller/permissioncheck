package io.github.simonschiller.permissioncheck.internal

import io.github.simonschiller.permissioncheck.internal.data.BasePermission
import io.github.simonschiller.permissioncheck.internal.data.Violation

internal class PermissionChecker {

    fun findViolations(
        baselinePermissions: Set<BasePermission>,
        manifestPermissions: Set<BasePermission>,
        strict: Boolean = false
    ): List<Violation> {
        val violations = mutableListOf<Violation>()

        // Find the differences
        val manifest = (manifestPermissions - baselinePermissions).sorted().toTypedArray()
        val baseline = (baselinePermissions - manifestPermissions).sorted().toTypedArray()
        var manifestIndex = 0
        var baselineIndex = 0

        // Process all differences and convert them to the right kind of violation
        while (manifestIndex < manifest.size && baselineIndex < baseline.size) {
            val nameOrder = manifest[manifestIndex].compareTo(baseline[baselineIndex])
            violations += when {
                nameOrder < 0 -> Violation.Added(manifest[manifestIndex++]) // Permissions added
                nameOrder > 0 -> Violation.Removed(baseline[baselineIndex++]) // Permission removed

                // Same permission -> max SDK changed
                else -> getSamePermissionViolation(manifest[manifestIndex++], baseline[baselineIndex++])
            }
        }

        // Process the remaining differences
        while (manifestIndex < manifest.size) {
            violations += Violation.Added(manifest[manifestIndex++])
        }
        while (baselineIndex < baseline.size) {
            violations += Violation.Removed(baseline[baselineIndex++])
        }

        // If we're not running in strict mode, filter out the strict violations
        return if (strict) {
            violations
        } else {
            violations.filterNot(Violation::strict)
        }
    }

    // Computes the correct violation for two permissions with the same name and tag
    private fun getSamePermissionViolation(manifest: BasePermission, baseline: BasePermission): Violation {
        val maxSdkOrder = (manifest.maxSdkVersion ?: Int.MAX_VALUE) - (baseline.maxSdkVersion ?: Int.MAX_VALUE)
        return when {
            maxSdkOrder < 0 -> Violation.MaxSdkDecreased(manifest, baseline.maxSdkVersion)
            maxSdkOrder > 0 -> Violation.MaxSdkIncreased(manifest, baseline.maxSdkVersion)
            else -> error("Could not determine violation for permissions $manifest and $baseline")
        }
    }
}
