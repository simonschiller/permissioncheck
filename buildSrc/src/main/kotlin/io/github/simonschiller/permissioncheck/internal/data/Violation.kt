package io.github.simonschiller.permissioncheck.internal.data

internal sealed class Violation(val message: String, val strict: Boolean = false) {

    data class Added(val permission: BasePermission) : Violation(
        "New permission '$permission' was added"
    )

    data class Removed(val permission: BasePermission) : Violation(
        "Permission '$permission' was removed", strict = true
    )

    data class MaxSdkIncreased(val permission: BasePermission, val from: Int?) : Violation(
        "Max SDK of permission '$permission' was increased (from ${from ?: "-"})"
    )

    data class MaxSdkDecreased(val permission: BasePermission, val from: Int?) : Violation(
        "Max SDK of permission '$permission' was decreased (from ${from ?: "-"})", strict = true
    )
}
