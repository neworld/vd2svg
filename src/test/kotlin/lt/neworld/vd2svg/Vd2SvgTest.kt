package lt.neworld.vd2svg

import com.intellij.rt.execution.junit.FileComparisonFailure
import lt.neworld.vd2svg.converter.Converter
import lt.neworld.vd2svg.resources.AndroidResourceParser
import lt.neworld.vd2svg.resources.ResourceCollector
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileFilter

/**
 * @author Andrius Semionovas
 * @since 2017-12-11
 */
class Vd2SvgTest {

    val testData = File("src/test/testData")
    val resources = File("src/test/resources/xml/android_resource_example.xml")

    val resourceCollector = ResourceCollector().apply {
        addResources(AndroidResourceParser(resources.inputStream()).values("color"))
    }
    val fixture = Converter(resourceCollector)

    @Test
    fun afterAapt() {
        runTests(File(testData, "after_aapt"))
    }
    @Test
    fun beforeAapt() {
        runTests(File(testData, "before_aapt"))
    }
    @Test
    fun colorAlias() {
        runTests(File(testData, "color_alias"))
    }

    private fun runTests(testDir: File) {
        testDir.listFiles(FileFilter { it.extension == "xml" })
                .forEach {
                    runTest(testDir, it)
                }
    }

    private fun runTest(testDir: File, source: File) {
        val expectedFile = File(testDir, source.nameWithoutExtension + ".svg")
        if (!expectedFile.exists()) {
            expectedFile.createNewFile()
        }
        expectedFile.setWritable(true)
        val expected = expectedFile.readText()

        val actual = ByteArrayOutputStream().use {
            fixture.convert(source.inputStream(), it)
            it
        }.toString()

        if (actual != expected) {
            throw FileComparisonFailure("Actual data differs from file content: ${expectedFile.name}", expected, actual, expectedFile.absolutePath)
        }
    }
}