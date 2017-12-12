package lt.neworld.vd2svg.converter

import lt.neworld.vd2svg.logProgress
import lt.neworld.vd2svg.resources.AndroidResourceParser
import lt.neworld.vd2svg.resources.ResourceCollector
import java.io.File
import java.util.*

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */
class Builder(val resourceFiles: List<File>) {

    private val allFiles by lazy {
        val result = mutableListOf<File>()
        val processing = ArrayDeque(resourceFiles)

        while (processing.isNotEmpty()) {
            val file = processing.removeFirst()
            if (file.isDirectory) {
                processing.addAll(file.listFiles())
            } else if (file.isFile && file.extension == "xml") {
                result += file
            }
        }

        result
    }

    fun build(): Converter {
        val collector = ResourceCollector()

        allFiles.map { Pair(it, it.inputStream()) }
                .map { (file, inputStream) -> Pair(file, AndroidResourceParser(inputStream)) }
                .forEach { (file, parser) ->
                    logProgress("Reading resources file: ${file.path}")
                    collector.addResources(parser.values("color"))
                }

        return Converter(collector)
    }
}