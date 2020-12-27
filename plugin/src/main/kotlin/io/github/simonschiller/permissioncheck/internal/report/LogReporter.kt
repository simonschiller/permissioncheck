package io.github.simonschiller.permissioncheck.internal.report

import io.github.simonschiller.permissioncheck.internal.data.Violation
import org.gradle.api.logging.Logger

internal class LogReporter(private val logger: Logger) : Reporter {

    override fun report(violations: Map<String, List<Violation>>) {
        val faultyVariants = violations.filterValues { it.isNotEmpty() }

        if (faultyVariants.isEmpty()) {
            logger.lifecycle("Found no violations, all permissions match the baseline")
        } else {
            faultyVariants.forEach { (variantName, variantViolations) ->
                logger.error("Found ${variantViolations.size} violation(s) for variant $variantName")
                variantViolations.forEach { violation ->
                    logger.error(violation.message)
                }
                logger.lifecycle("") // Separate different variants by blank lines
            }
        }
    }
}
