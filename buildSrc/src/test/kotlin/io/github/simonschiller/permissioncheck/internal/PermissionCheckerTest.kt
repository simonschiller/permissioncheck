package io.github.simonschiller.permissioncheck.internal

import io.github.simonschiller.permissioncheck.internal.data.BasePermission
import io.github.simonschiller.permissioncheck.internal.data.Permission
import io.github.simonschiller.permissioncheck.internal.data.Sdk23Permission
import io.github.simonschiller.permissioncheck.internal.data.Violation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PermissionCheckerTest {
    private val permissionChecker = PermissionChecker()

    @Test
    fun `No violations are found if permissions match`() {
        val baseline = setOf(
            Permission("android.permission.INTERNET"),
            Permission("android.permission.CAMERA", 28),
            Sdk23Permission("android.permission.ACCESS_COARSE_LOCATION"),
            Sdk23Permission("android.permission.ACCESS_FINE_LOCATION", 26)
        )
        val manifest = setOf(
            Permission("android.permission.INTERNET"),
            Permission("android.permission.CAMERA", 28),
            Sdk23Permission("android.permission.ACCESS_COARSE_LOCATION"),
            Sdk23Permission("android.permission.ACCESS_FINE_LOCATION", 26)
        )

        val violations = emptyList<Violation>()
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest))
    }

    @Test
    fun `All violations are found if there are differences`() {
        val baseline = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION", 26),
            Sdk23Permission("android.permission.CAMERA"),
            Permission("android.permission.INTERNET")
        )

        val manifest = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION", 27),
            Permission("android.permission.ACCESS_FINE_LOCATION"),
            Permission("android.permission.CAMERA"),
            Permission("android.permission.INTERNET")
        )

        val violations = listOf(
            Violation.MaxSdkIncreased(Permission("android.permission.ACCESS_COARSE_LOCATION", 27), 26),
            Violation.Added(Permission("android.permission.ACCESS_FINE_LOCATION")),
            Violation.Added(Permission("android.permission.CAMERA"))
        )
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest))
    }

    @Test
    fun `All violations are found if strict mode is enabled`() {
        val baseline = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION", 28),
            Sdk23Permission("android.permission.CAMERA"),
            Permission("android.permission.INTERNET")
        )

        val manifest = setOf(
            Permission("android.permission.ACCESS_COARSE_LOCATION", 27),
            Permission("android.permission.ACCESS_FINE_LOCATION"),
            Permission("android.permission.CAMERA"),
            Permission("android.permission.INTERNET")
        )

        val violations = listOf(
            Violation.MaxSdkDecreased(Permission("android.permission.ACCESS_COARSE_LOCATION", 27), 28),
            Violation.Added(Permission("android.permission.ACCESS_FINE_LOCATION")),
            Violation.Added(Permission("android.permission.CAMERA")),
            Violation.Removed(Sdk23Permission("android.permission.CAMERA"))
        )
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest, strict = true))
    }

    @Test
    fun `Adding permissions is not allowed`() {
        val baseline = emptySet<BasePermission>()
        val manifest = setOf(
            Sdk23Permission("android.permission.CAMERA"),
            Permission("android.permission.INTERNET")
        )

        val violations = listOf(
            Violation.Added(Sdk23Permission("android.permission.CAMERA")),
            Violation.Added(Permission("android.permission.INTERNET"))
        )
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest))
    }

    @Test
    fun `Removing permissions is allowed`() {
        val baseline = setOf(
            Sdk23Permission("android.permission.CAMERA"),
            Permission("android.permission.INTERNET")
        )
        val manifest = emptySet<BasePermission>()

        val violations = emptyList<BasePermission>()
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest))
    }

    @Test
    fun `Removing permissions is not allowed in strict mode`() {
        val baseline = setOf(
            Sdk23Permission("android.permission.CAMERA"),
            Permission("android.permission.INTERNET")
        )
        val manifest = emptySet<BasePermission>()

        val violations = listOf(
            Violation.Removed(Sdk23Permission("android.permission.CAMERA")),
            Violation.Removed(Permission("android.permission.INTERNET"))
        )
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest, strict = true))
    }

    @Test
    fun `Increasing the max SDK for a permission is not allowed`() {
        val baseline = setOf(
            Sdk23Permission("android.permission.CAMERA", 26),
            Permission("android.permission.INTERNET", 26)
        )
        val manifest = setOf(
            Sdk23Permission("android.permission.CAMERA", 27),
            Permission("android.permission.INTERNET", 27)
        )

        val violations = listOf(
            Violation.MaxSdkIncreased(Sdk23Permission("android.permission.CAMERA", 27), 26),
            Violation.MaxSdkIncreased(Permission("android.permission.INTERNET", 27), 26)
        )
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest))
    }

    @Test
    fun `Decreasing the max SDK for a permission is allowed`() {
        val baseline = setOf(
            Sdk23Permission("android.permission.CAMERA", 28),
            Permission("android.permission.INTERNET", 28)
        )
        val manifest = setOf(
            Sdk23Permission("android.permission.CAMERA", 27),
            Permission("android.permission.INTERNET", 27)
        )

        val violations = emptyList<Violation>()
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest))
    }

    @Test
    fun `Decreasing the max SDK for a permission is not allowed in strict mode`() {
        val baseline = setOf(
            Sdk23Permission("android.permission.CAMERA", 28),
            Permission("android.permission.INTERNET", 28)
        )
        val manifest = setOf(
            Sdk23Permission("android.permission.CAMERA", 27),
            Permission("android.permission.INTERNET", 27)
        )

        val violations = listOf(
            Violation.MaxSdkDecreased(Sdk23Permission("android.permission.CAMERA", 27), 28),
            Violation.MaxSdkDecreased(Permission("android.permission.INTERNET", 27), 28)
        )
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest, strict = true))
    }

    @Test
    fun `Permissions without max SDK are interpreted without bounds`() {
        val baseline = setOf(
            Sdk23Permission("android.permission.CAMERA", 26),
            Permission("android.permission.INTERNET", 26)
        )
        val manifest = setOf(
            Sdk23Permission("android.permission.CAMERA"),
            Permission("android.permission.INTERNET")
        )

        val violations = listOf(
            Violation.MaxSdkIncreased(Sdk23Permission("android.permission.CAMERA"), 26),
            Violation.MaxSdkIncreased(Permission("android.permission.INTERNET"), 26)
        )
        assertEquals(violations, permissionChecker.findViolations(baseline, manifest))
    }
}
