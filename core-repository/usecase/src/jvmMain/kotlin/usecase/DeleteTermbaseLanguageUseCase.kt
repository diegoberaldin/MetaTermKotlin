package usecase

import repository.EntryRepository
import repository.LanguagePropertyValueRepository
import repository.LanguageRepository
import repository.TermPropertyValueRepository
import repository.TermRepository

class DeleteTermbaseLanguageUseCase(
    private val entryRepository: EntryRepository,
    private val languageRepository: LanguageRepository,
    private val languagePropertyValueRepository: LanguagePropertyValueRepository,
    private val termRepository: TermRepository,
    private val termPropertyValueRepository: TermPropertyValueRepository,
) {

    suspend operator fun invoke(code: String, termbaseId: Int) {
        val language = languageRepository.getByCode(code, termbaseId)
        if (language != null) {
            val entries = entryRepository.getAll(termbaseId)
            for (entry in entries) {
                val terms = termRepository.getAll(entryId = entry.id)
                for (term in terms) {
                    deleteTerm(term.id)
                }

                deleteLanguageProperties(languageId = language.id, entryId = entry.id)
            }

            languageRepository.delete(language)
        }

        // removes stale entries without terms
        val entries = entryRepository.getAll(termbaseId)
        for (entry in entries) {
            if (termRepository.getAll(entryId = entry.id).isEmpty()) {
                entryRepository.delete(entry)
            }
        }
    }

    private suspend fun deleteLanguageProperties(languageId: Int, entryId: Int) {
        val properties = languagePropertyValueRepository.getAll(languageId, entryId)
        for (property in properties) {
            languagePropertyValueRepository.delete(property)
        }
    }

    private suspend fun deleteTerm(termId: Int) {
        deleteTermProperties(termId)

        val term = termRepository.getById(termId)
        if (term != null) {
            termRepository.delete(term)
        }
    }

    private suspend fun deleteTermProperties(termId: Int) {
        val properties = termPropertyValueRepository.getAll(termId)
        for (property in properties) {
            termPropertyValueRepository.delete(property)
        }
    }
}
