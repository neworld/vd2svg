package lt.neworld.vd2svg.resources

/**
 * @author Andrius Semionovas
 * @since 2017-11-22
 */
class ResourceCollector {
    private val resources = mutableMapOf<String, String>()

    fun addResources(values: Iterable<ResourceEntry>) {
        resources.putAll(values)
    }

    fun getValue(name: String): String? {
        var curName = name

        do {
            val value = resources[curName] ?: return null

            if (!value.startsWith("@")) {
                return value
            }

            curName = value.split("/").last()
        } while (true)
    }
}