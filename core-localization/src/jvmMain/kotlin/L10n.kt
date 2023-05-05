object L10n {

    private val default = DefaultLocalization()

    fun setLanguage(lang: String) {
        default.setLanguage(lang)
    }

    fun get(key: String): String = default.get(key)
}


fun String.localized(): String {
    return L10n.get(this)
}