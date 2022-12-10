package lt.neworld.vd2svg.converter

import lt.neworld.vd2svg.resources.ResourceCollector
import lt.neworld.vd2svg.xml.get
import lt.neworld.vd2svg.xml.iterable
import org.w3c.dom.*
import java.io.InputStream
import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
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
    private val transformer by lazy {
        val factory = TransformerFactory.newInstance()
        factory.setAttribute("indent-number", 4)
        val transformer = factory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer
    }

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

        doc.getElementsByTagNameNS(null, "clip-path").iterable.map { it as Element }.forEach {
            val parent = it.parentNode as? Element
            val clipPathId = convertClipPathElement(doc, it)
            parent?.setAttribute("clip-path", "url(#$clipPathId)")
            parent?.removeChild(it)
        }

        fixEmptyNamespace(doc.documentElement)
    }

    private fun convertClipPathElement(doc: Document, element: Element): String? {
        val index = clipPathCount(doc)
        val id = "_clippath_$index"
        val cp = createClipPath(doc, element, id)

        addElementToDefSection(doc, cp)

        return id
    }

    private fun clipPathCount(doc: Document): Int {
        return doc.getElementsByTagName("clipPath").length
    }

    private fun addElementToDefSection(doc: Document, element: Element) {
        val defSection = doc.getElementById(DEFS_SECTION) ?: createDefSection(doc)
        defSection.appendChild(element)
    }

    private fun createDefSection(doc: Document): Element {
        val element = doc.createElement("defs")
        element.setId(DEFS_SECTION)
        doc.documentElement.appendChild(element)

        return element
    }

    private fun createClipPath(doc: Document, androidClipPath: Element, id: String): Element {
        val pathData = androidClipPath.attributes.get(ANDROID_NS, "pathData")
        val pathElement = doc.createElement("path")
        pathElement.setAttribute("d", pathData)

        val clipPathElement = doc.createElement("clipPath")
        clipPathElement.setId(id)
        clipPathElement.appendChild(pathElement)

        return clipPathElement
    }

    private fun parseColor(color: String): Pair<String, Float?> {
        var colorHex: String = color
        if (color.startsWith("@")) {
            val name = color.split("/").last()
            val resourceHex = colors.getValue(name)
            if (resourceHex == null) {
                throw IllegalArgumentException("Color $name does not exists")
            }

            colorHex = resourceHex
        }

        return parseColorHex(colorHex)
    }

    private fun parseColorHex(colorHex: String): Pair<String, Float?> {
        val color: String
        if (colorHex.length < 6) {
            color = colorHex.takeLast(3)
        } else {
            color = colorHex.takeLast(6)
        }

        var opacity: Float? = null
        if (colorHex.length == 9) {
            opacity = colorHex.drop(1).take(2).toInt(16).toFloat() / 255.0f
        }

        return Pair("#$color", opacity)
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
        val strokeOpacity = attributes.get(ANDROID_NS, "strokeAlpha")
        val strokeLineCap = attributes.get(ANDROID_NS, "strokeLineCap")
        val strokeLineJoin = attributes.get(ANDROID_NS, "strokeLineJoin")

        if (strokeColor != null) {
            val (strokeColorHex, _) = parseColor(strokeColor)
            setAttribute("stroke", strokeColorHex)
        }

        if (strokeWidth != null) {
            setAttribute("stroke-width", strokeWidth)
        }

        if (strokeOpacity != null) {
            setAttribute("stroke-opacity", strokeOpacity)
        }

        if (strokeLineCap != null) {
            setAttribute("stroke-linecap", strokeLineCap)
        }

        if (strokeLineJoin != null) {
            setAttribute("stroke-linejoin", strokeLineJoin)
        }

        removeAttributeNS(ANDROID_NS, "strokeColor")
        removeAttributeNS(ANDROID_NS, "strokeWidth")
        removeAttributeNS(ANDROID_NS, "strokeAlpha")
        removeAttributeNS(ANDROID_NS, "strokeLineCap")
        removeAttributeNS(ANDROID_NS, "strokeLineJoin")
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
        val fillColorName = attributes.get(ANDROID_NS, "fillColor")
        var fillColorHex: String? = null
        var fillAlpha: Float = attributes.get(ANDROID_NS, "fillAlpha")?.toFloatOrNull() ?: 1.0f

        if (fillColorName != null) {
            val (colorHex, androidAlpha) = parseColor(fillColorName)
            if (androidAlpha != null) {
                fillAlpha *= androidAlpha
            }
            fillColorHex = colorHex
        }

        if (fillAlpha != 1.0f) {
            setAttribute("fill-opacity", fillAlpha.toString())
        }

        if (fillColorHex != null) {
            setAttribute("fill", fillColorHex)
        }

        removeAttributeNS(ANDROID_NS, "fillColor")
        removeAttributeNS(ANDROID_NS, "fillAlpha")
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

    private fun Element.appendChildNodes(nodes: List<Node>) {
        nodes.forEach {
            appendChild(it)
        }
    }

    private fun Element.removeAllChildNodes() {
        childNodes.iterable.forEach {
            removeChild(it)
        }
    }

    private fun Element.id(): String {
        return getAttribute("id")
    }

    private fun Element.setId(id: String) {
        setAttribute("id", id)
        setIdAttribute("id", true)
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

        const val DEFS_SECTION = "svg-definitions"
    }
}
