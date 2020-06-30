package io.github.simonschiller.permissioncheck.internal.util

import org.w3c.dom.Element
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory

internal fun createDocumentBuilder(): DocumentBuilder {
    val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
    return factory.newDocumentBuilder()
}

internal fun createTransformer() = TransformerFactory.newInstance().newTransformer().apply {
    setOutputProperty(OutputKeys.INDENT, "yes")
    setOutputProperty(OutputKeys.METHOD, "xml")
    setOutputProperty(OutputKeys.ENCODING, "UTF-8")
    setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
}

internal fun NodeList.forEach(action: (Element) -> Unit) {
    for (i in 0 until length) {
        val item = item(i)
        if (item is Element) {
            action(item)
        }
    }
}
