@file:JvmName("Vd2Svg")

package lt.neworld.vd2svg

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import lt.neworld.vd2svg.processor.Builder
import java.io.File
import java.nio.file.FileSystems
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Andrius Semionovas
 * @since 2017-11-21
 */

private const val PROGRAM_NAME = "vd2svg"

fun main(args: Array<String>) {
    return mainBody(PROGRAM_NAME) {
        ParsedArgs(ArgParser(args)).run {
            Builder(this).build().process()
        }
    }
}

class ParsedArgs(parser: ArgParser) {
    val verbose by parser.flagging("-v", "--verbose",
            help = "Verbose mode. Causes $PROGRAM_NAME to print info messages"
    )

    val quiet by parser.flagging("-q", "--quiet",
            help = "Quiet mode. Disables progress output"
    )

    val output: File? by parser.storing(
            "-o", "--output",
            help = "Path to the output directory. By default, output is put together with the input files",
            transform = { File(this) }
    ).default(null)

    val resources by parser.adding(
            "-r", "--resources",
            help = "File or directory containing android resources for color parsing",
            transform = { File(this) }
    )

    val input by parser.positionalList("INPUT",
            sizeRange = 1..Int.MAX_VALUE,
            help = "Input file mask. For example '*.xml'",
            transform = { FileSystems.getDefault().getPathMatcher("glob:" + this) }
    )
}