package io.github.simonschiller.permissioncheck.internal.report

import io.github.simonschiller.permissioncheck.internal.data.Violation
import org.gradle.api.logging.Logger

internal class LogReporter(private val logger: Logger) : Reporter {

    override fun report(violations: List<Violation>) {
        if (violations.isEmpty()) {
            logger.lifecycle("Found no violations, all permissions match the baseline")
        } else {
            logger.error("Found ${violations.size} violation(s) while checking permissions")
            violations.forEach { violation ->
                logger.error(violation.message)
            }
        }
    }
}
