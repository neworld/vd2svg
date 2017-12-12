package lt.neworld.vd2svg.processor

import com.xenomachina.argparser.SystemExitException
import lt.neworld.vd2svg.converter.Converter
import lt.neworld.vd2svg.logProgress
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.PathMatcher

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */
class Processor(val converter: Converter, val input: List<PathMatcher>, val output: File?) {
    fun process() {
        prepareOutput(output)

        File(".").walkTopDown()
                .filter { file -> input.any { it.matches(file.relativeTo(File(".")).toPath()) } }
                .filter { file -> file.readText().contains("<vector") }
                .forEach { file ->
                    logProgress("Processing: $file")

                    val outputStream = createOutputStream(file)
                    converter.convert(file.inputStream(), outputStream)
                    outputStream.close()
                }
    }

    private fun createOutputStream(input: File): OutputStream {
        val filename = input.nameWithoutExtension
        val outputDir = output ?: input.parentFile

        val fileOutput = File(outputDir, "$filename.svg")

        logProgress("Save to: $fileOutput")

        return FileOutputStream(fileOutput)
    }

    private fun prepareOutput(output: File?) {
        output ?: return

        if (output.isFile) {
            throw SystemExitException("output is not directory", 1)
        }
        if (!output.exists()) {
            logProgress("Creating output dir: ${output.path}")
            output.mkdirs() || throw SystemExitException("Failed create output dir", 1)
        }
    }
}