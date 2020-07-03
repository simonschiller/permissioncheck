package io.github.simonschiller.permissioncheck.internal.report

import io.github.simonschiller.permissioncheck.internal.data.Violation
import io.github.simonschiller.permissioncheck.internal.util.appendElement
import io.github.simonschiller.permissioncheck.internal.util.createDocumentBuilder
import io.github.simonschiller.permissioncheck.internal.util.writeToFile
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File

internal class HtmlReporter(private val reportFile: File, private val variantName: String) : Reporter {

    // Only HTML report files are allowed
    init {
        if (!reportFile.extension.equals("html", ignoreCase = true)) {
            throw IllegalArgumentException("The HTML report has to be a .html file")
        }
    }

    override fun report(violations: List<Violation>) {
        val css = javaClass.getResourceAsStream("/bootstrap.min.css").use { inputStream ->
            inputStream.bufferedReader().readText()
        }

        val document = createDocumentBuilder().newDocument()
        document.appendElement("html") {
            setAttribute("lang", "en")

            appendElement("head") {
                appendTextElement("title", "PermissionCheck Report")
                appendElement("meta") {
                    setAttribute("name", "viewport")
                    setAttribute("content", "width=device-width, initial-scale=1, shrink-to-fit=no")
                }
                appendTextElement("style", css)
            }

            appendElement("body") {
                setAttribute("class", "bg-light")
                appendElement("div") {
                    setAttribute("class", "container")
                    appendTextElement("h1", "PermissionCheck Report", "mt-4 mb-4")
                    appendCard(violations)
                }
            }
        }

        // Write HTML report to disk
        document.writeToFile(reportFile, omitXmlDeclaration = true)
    }

    private fun Element.appendCard(violations: List<Violation>) = appendElement("div") {
        setAttribute("class", "card mt-4 mb-4 bg-dark")

        // Card header
        appendElement("div") {
            setAttribute("class", "card-header d-flex justify-content-between align-items-center text-light")
            appendTextElement("b", variantName) // Card title
            appendTextElement("span", violations.size.toString(), "badge badge-pill badge-light") // Counter badge
        }

        // Card body (all violations)
        appendElement("div") {
            setAttribute("class", "list-group list-group-flush")
            if (violations.isEmpty()) {
                appendEmptyMessage()
            } else {
                violations.forEach { violation -> appendViolation(violation) }
            }
        }
    }

    private fun Element.appendEmptyMessage() = appendElement("div") {
        setAttribute("class", "list-group-item")
        appendTextElement("h5", "No violations found") // Header
        appendTextElement("p", "All permissions from the manifest match the baseline.") // Description
    }

    private fun Element.appendViolation(violation: Violation) = appendElement("div") {
        setAttribute("class", "list-group-item")

        // Violation header
        appendElement("div") {
            setAttribute("class", "d-flex justify-content-between align-items-center")
            appendTextElement("h5", violation.title) // Header text
            if (violation.strict) {
                appendTextElement("span", "strict", "badge badge-danger badge-pill") // Header badge
            }
        }

        appendTextElement("p", violation.description) // Description
        appendViolationDetails(violation) // Details
    }

    private fun Element.appendViolationDetails(violation: Violation) = appendElement("div") {
        setAttribute("class", "list-group list-group-flush")

        val oldPermission = when (violation) {
            is Violation.Added -> null
            is Violation.Removed -> violation.permission
            is Violation.MaxSdkIncreased -> violation.permission.copy(maxSdkVersion = violation.from)
            is Violation.MaxSdkDecreased -> violation.permission.copy(maxSdkVersion = violation.from)
        }

        // Removed code snippet
        if (oldPermission != null) {
            appendTextElement("pre", oldPermission.toString(), "list-group-item list-group-item-danger m-0")
        }

        val newPermission = when (violation) {
            is Violation.Added -> violation.permission
            is Violation.Removed -> null
            is Violation.MaxSdkIncreased -> violation.permission
            is Violation.MaxSdkDecreased -> violation.permission
        }

        // Added code snippet
        if (newPermission != null) {
            appendTextElement("pre", newPermission.toString(), "list-group-item list-group-item-success m-0")
        }
    }

    private fun Node.appendTextElement(tag: String, text: String, classes: String? = null) {
        appendElement(tag) {
            if (classes != null) {
                setAttribute("class", classes)
            }
            appendChild(ownerDocument.createTextNode(text))
        }
    }
}
