package lt.neworld.vd2svg.resources

import lt.neworld.vd2svg.xml.get
import lt.neworld.vd2svg.xml.iterable
import lt.neworld.vd2svg.xml.iterator
import org.w3c.dom.Node
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */

typealias ResourceEntry = Pair<String, String>

class AndroidResourceParser(val inputStream: InputStream) {

    private val factory = DocumentBuilderFactory.newInstance()
    private val builder = factory.newDocumentBuilder()

    private val doc by lazy {
        builder.parse(inputStream).apply {
            documentElement.normalize()
        }
    }

    fun values(type: String): Iterable<ResourceEntry> {
        return doc.getElementsByTagName(type).iterable.map {
            val name = it.attributes["name"]!!
            val value = it.textContent
            ResourceEntry(name, value)
        }
    }
}
