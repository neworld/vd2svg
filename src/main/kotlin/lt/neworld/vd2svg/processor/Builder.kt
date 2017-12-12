package lt.neworld.vd2svg.processor

import lt.neworld.vd2svg.ParsedArgs
import lt.neworld.vd2svg.converter.Builder
import lt.neworld.vd2svg.converter.Converter

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */
class Builder(val args: ParsedArgs) {
    fun build(): Processor {
        val converter: Converter = Builder(args.resources).build()

        return Processor(converter, args.input, args.output)
    }
}