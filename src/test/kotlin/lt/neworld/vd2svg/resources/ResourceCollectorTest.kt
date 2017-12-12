package lt.neworld.vd2svg.resources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */
class ResourceCollectorTest {
    val fixture = ResourceCollector()

    @Test
    fun empty() {
        assertNull(fixture.getValue("foo"))
    }

    @Test
    fun dontHave() {
        fixture.addResources(createResouce("foo", "bar"))

        assertNull(fixture.getValue("bar"))
    }

    @Test
    fun haveValue() {
        fixture.addResources(createResouce("foo", "bar"))

        assertEquals("bar", fixture.getValue("foo"))
    }

    @Test
    fun alias_getNotExisting() {
        fixture.addResources(createResouce("foo", "bar"))
        fixture.addResources(createResouce("alias", "@color/foo"))

        assertEquals(null, fixture.getValue("bar"))
    }

    @Test
    fun alias_hasValue() {
        fixture.addResources(createResouce("foo", "bar"))
        fixture.addResources(createResouce("alias", "@color/foo"))

        assertEquals("bar", fixture.getValue("alias"))
    }

    @Test
    fun alias_pointNowhere_null() {
        fixture.addResources(createResouce("foo", "bar"))
        fixture.addResources(createResouce("alias", "@color/not_exists"))

        assertEquals(null, fixture.getValue("alias"))
    }

    private fun createResouce(name: String, value: String): Iterable<ResourceEntry> {
        return listOf(ResourceEntry(name, value))
    }
}