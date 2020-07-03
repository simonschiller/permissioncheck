package io.github.simonschiller.permissioncheck.internal.report

import io.github.simonschiller.permissioncheck.internal.data.Violation

internal interface Reporter {
    fun report(violations: List<Violation>)
}
