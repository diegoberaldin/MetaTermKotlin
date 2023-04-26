package repository

import dao.LanguageDAO
import data.LanguageModel
import java.util.*

class LanguageRepository(
    private val languageDAO: LanguageDAO,
) {

    suspend fun create(model: LanguageModel): Int = languageDAO.create(model)

    suspend fun getAll(termbaseId: Int): List<LanguageModel> = languageDAO.getAll(termbaseId)

    suspend fun getByCode(code: String, termbaseId: Int): LanguageModel? = languageDAO.getByCode(code, termbaseId)

    suspend fun getById(languageId: Int): LanguageModel? = languageDAO.getById(languageId)

    suspend fun delete(model: LanguageModel) {
        languageDAO.delete(model)
    }

    fun getDefaultLanguages() = listOf(
        LanguageModel(code = Locale.ENGLISH.language),
        LanguageModel(code = Locale.FRENCH.language),
        LanguageModel(code = Locale.GERMAN.language),
        LanguageModel(code = Locale.ITALIAN.language),
        LanguageModel(code = "bg"),
        LanguageModel(code = "cs"),
        LanguageModel(code = "da"),
        LanguageModel(code = "el"),
        LanguageModel(code = "es"),
        LanguageModel(code = "et"),
        LanguageModel(code = "fi"),
        LanguageModel(code = "ga"),
        LanguageModel(code = "hr"),
        LanguageModel(code = "hu"),
        LanguageModel(code = "lt"),
        LanguageModel(code = "lv"),
        LanguageModel(code = "mt"),
        LanguageModel(code = "nl"),
        LanguageModel(code = "pl"),
        LanguageModel(code = "pt"),
        LanguageModel(code = "ro"),
        LanguageModel(code = "sk"),
        LanguageModel(code = "sl"),
        LanguageModel(code = "sw"),
    )
}
