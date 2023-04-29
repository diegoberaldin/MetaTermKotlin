import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

internal object L10n {

    @OptIn(ExperimentalSerializationApi::class)
    private val localizables: List<LocalizableString> by lazy {
        L10n::class.java.getResourceAsStream("strings.json")?.use {
            Json.decodeFromStream(it)
        } ?: emptyList()
    }

    internal fun get(key: String): String {
        return localizables.firstOrNull { it.key == key }?.value ?: key
    }
}

fun String.localized(): String {
    return L10n.get(this)
}
