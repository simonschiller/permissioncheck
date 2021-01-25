package io.github.simonschiller.permissioncheck.internal.report

import io.github.simonschiller.permissioncheck.internal.data.Violation
import io.github.simonschiller.permissioncheck.internal.util.appendElement
import io.github.simonschiller.permissioncheck.internal.util.createDocumentBuilder
import io.github.simonschiller.permissioncheck.internal.util.writeToFile
import java.io.File

internal class XmlReporter(private val reportFile: File) : Reporter {

    // Only XML report files are allowed
    init {
        if (!reportFile.extension.equals("xml", ignoreCase = true)) {
            throw IllegalArgumentException("The XML report has to be a .xml file")
        }
    }

    override fun report(violations: Map<String, List<Violation>>) {
        val document = createDocumentBuilder().newDocument()
        document.appendElement("violations") {
            violations.forEach { (variantName, variantViolations) ->
                appendElement("variant") {
                    setAttribute("name", variantName)
                    variantViolations.forEach { violation ->
                        appendChild(violation.toXmlElement(document))
                    }
                }
            }
        }

        // Write XML report to disk
        document.writeToFile(reportFile)
    }
}
