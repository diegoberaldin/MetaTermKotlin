interface Localization {
    fun get(key: String): String

    fun setLanguage(lang: String)

    fun getLanguage(): String
}