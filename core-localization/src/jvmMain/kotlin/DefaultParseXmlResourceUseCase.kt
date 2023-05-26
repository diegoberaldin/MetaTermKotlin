import java.io.InputStream
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory

internal class DefaultParseXmlResourceUseCase : ParseXmlResourceUseCase {

    companion object {
        private const val ELEM_STRING = "string"
        private const val ATTR_NAME = "name"
        private const val ATTR_TRANSLATABLE = "translatable"
    }

    override operator fun invoke(inputStream: InputStream): List<LocalizableString> {
        return runCatching {
            val res = mutableListOf<LocalizableString>()
            val inputFactory = XMLInputFactory.newInstance()
            val eventReader = inputFactory.createXMLEventReader(inputStream)

            while (eventReader.hasNext()) {
                val evt = eventReader.nextEvent()
                if (evt.isStartElement) {
                    val startElement = evt.asStartElement()
                    val elemName = startElement.name.localPart
                    if (elemName == ELEM_STRING) {
                        val key = runCatching {
                            startElement.getAttributeByName(QName(ATTR_NAME)).value
                        }.getOrElse { "" }
                        val translatable = runCatching {
                            startElement.getAttributeByName(QName(ATTR_TRANSLATABLE)).value.toBoolean()
                        }.getOrElse { true }
                        val text = runCatching {
                            eventReader.nextEvent().asCharacters().data
                        }.getOrElse { "" }.sanitize()
                        val segment = LocalizableString(
                            key = key,
                            value = text,
                        )
                        res += segment
                    }
                }
            }
            res
        }.getOrElse { emptyList() }
    }

    private fun String.sanitize(): String {
        val substitutionList = listOf(
            "\\'" to "'",
            "\\n" to "\n",
        )
        return substitutionList.fold(this) { res, it ->
            res.replace(it.first, it.second)
        }
    }
}
