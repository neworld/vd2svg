package lt.neworld.vd2svg.resources

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */
class AndroidResourceParserTest {

    val example = javaClass.classLoader.getResourceAsStream("xml/android_resource_example.xml")
    val fixture = AndroidResourceParser(example)

    @Test
    fun values_notExisting_empty() {
        assertEquals(emptyList<ResourceEntry>(), fixture.values("float").toList())
    }

    @Test
    fun values_string_oneValue() {
        assertEquals(listOf("hello" to "world"), fixture.values("string").toList())
    }

    @Test
    fun values_colors() {
        assertEquals(setOf("white" to "#FFF", "alias" to "@color/white"), fixture.values("color").toSet())
    }
}