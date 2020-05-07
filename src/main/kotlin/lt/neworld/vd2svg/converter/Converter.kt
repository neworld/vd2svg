package lt.neworld.vd2svg.converter

import lt.neworld.vd2svg.resources.ResourceCollector
import lt.neworld.vd2svg.xml.get
import lt.neworld.vd2svg.xml.iterable
import org.w3c.dom.*
import java.io.InputStream
import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */
class Converter(val colors: ResourceCollector) {

    private val builder by lazy {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.newDocumentBuilder()
    }

    private val transformer: Transformer
        get() = TransformerFactory.newInstance().newTransformer()

    fun convert(input: InputStream, outputStream: OutputStream) {
        val doc = builder.parse(input)

        if (doc.documentElement.nodeName != "vector") {
            return
        }

        convert(doc)

        val source = DOMSource(doc)
        val result = StreamResult(outputStream)
        transformer.transform(source, result)
    }

    private fun convert(doc: Document) {
        with(doc.documentElement) {
            removeAttributeNS(ANDROID_NS, "width")
            removeAttributeNS(ANDROID_NS, "height")

            attributes.rename(ANDROID_NS, "viewportHeight", SVG_NS, "height")
            attributes.rename(ANDROID_NS, "viewportWidth", SVG_NS, "width")

            rename("svg", SVG_NS)
        }

        doc.getElementsByTagNameNS(null, "group").iterable.map { it as Element }.forEach {
            it.rename("g", null)
            it.fix()
        }

        doc.getElementsByTagNameNS(null, "path").iterable.map { it as Element }.forEach {
            it.attributes.rename(ANDROID_NS, "pathData", SVG_NS, "d")
            it.attributes.rename(ANDROID_NS, "fillType", SVG_NS, "fill-rule")
            it.fix()
        }

        fixEmptyNamespace(doc.documentElement)
    }

    private fun fixEmptyNamespace(node: Node) {
        node.childNodes.iterable.forEach {
            if (it is Element) {
                it.rename(it.tagName, SVG_NS)
                fixEmptyNamespace(it)
            }
        }
    }

    private fun Element.fix() {
        fixTranslate()
        fixFill()
        fixRotate()
        fixScale()
        fixStroke()
    }

    private fun Element.fixStroke() {
        val strokeColor = attributes.get(ANDROID_NS, "strokeColor")
        val strokeWidth = attributes.get(ANDROID_NS, "strokeWidth")

        if (strokeColor != null) {
            setAttribute("stroke", strokeColor)
        }

        if (strokeWidth != null) {
            setAttribute("stroke-width", strokeWidth)
        }

        removeAttributeNS(ANDROID_NS, "strokeColor")
        removeAttributeNS(ANDROID_NS, "strokeWidth")
    }

    private fun Element.fixTranslate() {
        val translateX = attributes.get(ANDROID_NS, "translateX")
        val translateY = attributes.get(ANDROID_NS, "translateY")

        if (translateX != null) {
            val translate = translateY?.let { y -> "translate($translateX, $y)" } ?: "translate($translateX)"
            appendAttribute("transform", translate)
        }

        removeAttributeNS(ANDROID_NS, "translateX")
        removeAttributeNS(ANDROID_NS, "translateY")
    }

    private fun Element.fixScale() {
        val scaleX = attributes.get(ANDROID_NS, "scaleX")
        val scaleY = attributes.get(ANDROID_NS, "scaleY")

        if (scaleX != null || scaleY != null) {
            val scaleX = scaleX ?: "1"
            val scaleY = scaleY ?: "1"

            appendAttribute("transform", "scale($scaleX, $scaleY)")
        }

        removeAttributeNS(ANDROID_NS, "scaleX")
        removeAttributeNS(ANDROID_NS, "scaleY")
    }

    private fun Element.fixFill() {
        attributes.rename(ANDROID_NS, "fillColor", SVG_NS, "fill")

        var value = attributes.get(null, "fill") ?: return

        if (value.startsWith("@")) {
            val name = value.split("/").last()
            setAttribute("fill", colors.getValue(name))
        }

        value = attributes.get(null, "fill") ?: return

        if (value.length == 9) {
            val alpha = value.drop(1).take(2).toInt(16).toFloat() / 255.0
            val color = value.drop(3)

            setAttribute("fill", "#$color")
            setAttribute("fill-opacity", alpha.toString())
        }
    }

    private fun Element.fixRotate() {
        val pivotX = attributes.get(ANDROID_NS, "pivotX")
        val pivotY = attributes.get(ANDROID_NS, "pivotY")
        val rotation = attributes.get(ANDROID_NS, "rotation")

        if (rotation == null) return

        if (pivotX != null || pivotY != null) {
            appendAttribute("transform", "rotation($rotation, ${pivotX!!} ${pivotY!!})")
        } else {
            appendAttribute("transform", "rotation($rotation)")
        }

        removeAttributeNS(ANDROID_NS, "pivotX")
        removeAttributeNS(ANDROID_NS, "pivotY")
        removeAttributeNS(ANDROID_NS, "rotation")
    }

    private fun Element.appendAttribute(name: String, value: String, delimiter: String = " ") {
        val current = attributes.get(null, name)

        if (current == null) {
            setAttribute(name, value)
        } else {
            setAttribute(name, "$current$delimiter$value")
        }
    }

    private fun NamedNodeMap.rename(nameSpaceUri: String, old: String, newNameSpaceUri: String?, new: String) {
        val node = getNamedItemNS(nameSpaceUri, old) as Attr? ?: return
        with(node.ownerElement) {
            removeAttributeNS(nameSpaceUri, old)
            setAttribute(new, node.value)
        }
    }

    private fun Node.rename(new: String, namespaceUri: String?) {
        ownerDocument.renameNode(this, namespaceUri, new)
    }

    companion object {
        const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
        const val SVG_NS = "http://www.w3.org/2000/svg"
    }
}
