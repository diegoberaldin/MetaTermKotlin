import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@OptIn(ExperimentalSerializationApi::class)
class DefaultLocalization : Localization {

    private val defaultValues: List<LocalizableString> =
        DefaultLocalization::class.java.getResourceAsStream("l10n/en/strings.json")?.use {
            Json.decodeFromStream(it)
        } ?: emptyList()
    private var localizables: List<LocalizableString> = emptyList()

    override fun setLanguage(lang: String) {
        localizables = DefaultLocalization::class.java.getResourceAsStream("l10n/$lang/strings.json")?.use {
            Json.decodeFromStream(it)
        } ?: defaultValues
    }

    override fun get(key: String) = localizables.firstOrNull { it.key == key }?.value
        ?: defaultValues.firstOrNull { it.key == key }?.value
        ?: key
}



