import java.io.InputStream

internal class DefaultLocalization(
    private val parseResource: ParseXmlResourceUseCase = DefaultParseXmlResourceUseCase()
) : Localization {

    private val defaultValues: List<LocalizableString> =
        DefaultLocalization::class.java.getResourceAsStream(getPathForL10nResource("en"))?.use {
            load(it)
        } ?: emptyList()
    private var localizables: List<LocalizableString> = emptyList()

    override fun setLanguage(lang: String) {
        val path = getPathForL10nResource(lang)
        localizables = DefaultLocalization::class.java.getResourceAsStream(path)?.use {
            load(it)
        } ?: defaultValues
    }

    override fun getLanguage(): String = "lang".localized()

    override fun get(key: String) = localizables.firstOrNull { it.key == key }?.value
        ?: defaultValues.firstOrNull { it.key == key }?.value
        ?: key

    private fun getPathForL10nResource(lang: String) = "l10n/$lang/strings.xml"

    private fun load(inputStream: InputStream): List<LocalizableString> = parseResource(inputStream)
}
