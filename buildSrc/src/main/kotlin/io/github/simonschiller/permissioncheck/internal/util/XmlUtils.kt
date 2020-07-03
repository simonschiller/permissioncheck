package io.github.simonschiller.permissioncheck.internal.util

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

internal fun createDocumentBuilder(): DocumentBuilder {
    val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
    return factory.newDocumentBuilder()
}

internal fun Document.writeToFile(target: File, omitXmlDeclaration: Boolean = false) {
    val transformer = TransformerFactory.newInstance().newTransformer().apply {
        setOutputProperty(OutputKeys.INDENT, "yes")
        setOutputProperty(OutputKeys.METHOD, "xml")
        setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
        if (omitXmlDeclaration) {
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        }
    }

    target.parentFile.mkdirs()
    FileOutputStream(target).use { outputStream ->
        transformer.transform(DOMSource(this), StreamResult(outputStream))
    }
}

internal fun NodeList.forEach(action: (Element) -> Unit) {
    for (i in 0 until length) {
        val item = item(i)
        if (item is Element) {
            action(item)
        }
    }
}

internal fun Node.appendElement(tag: String, block: Element.() -> Unit) {
    val document = when (this) {
        is Document -> this
        else -> ownerDocument
    }
    val element = document.createElement(tag)
    appendChild(element)
    element.apply(block)
}
