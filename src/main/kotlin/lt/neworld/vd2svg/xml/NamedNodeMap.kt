package lt.neworld.vd2svg.xml

import org.w3c.dom.NamedNodeMap

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */

operator fun NamedNodeMap.get(name: String): String? = getNamedItem(name)?.nodeValue

fun NamedNodeMap.get(namespaceUri: String?, name: String): String? = getNamedItemNS(namespaceUri, name)?.nodeValue
