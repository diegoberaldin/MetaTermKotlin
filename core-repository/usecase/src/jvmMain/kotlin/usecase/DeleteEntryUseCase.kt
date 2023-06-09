package usecase

import data.EntryModel
import data.PropertyType
import repo.EntryPropertyValueRepository
import repo.EntryRepository
import repo.LanguagePropertyValueRepository
import repo.LanguageRepository
import repo.PropertyRepository
import repo.TermPropertyValueRepository
import repo.TermRepository
import java.io.File

class DeleteEntryUseCase(
    private val entryRepository: EntryRepository,
    private val entryPropertyValueRepository: EntryPropertyValueRepository,
    private val propertyRepository: PropertyRepository,
    private val languageRepository: LanguageRepository,
    private val languagePropertyValueRepository: LanguagePropertyValueRepository,
    private val termRepository: TermRepository,
    private val termPropertyValueRepository: TermPropertyValueRepository,
) {

    suspend operator fun invoke(entry: EntryModel) {
        val propertyValues = entryPropertyValueRepository.getAll(entry.id)
        for (propertyValue in propertyValues) {
            val property = propertyRepository.getById(propertyValue.propertyId)
            if (property?.type == PropertyType.IMAGE) {
                propertyValue.value?.let { File(it) }?.delete()
            }
            entryPropertyValueRepository.delete(propertyValue)
        }
        val languages = languageRepository.getAll(entry.termbaseId)
        for (language in languages) {
            val langPropertyValues = languagePropertyValueRepository.getAll(language.id, entry.id)
            for (propertyValue in langPropertyValues) {
                val property = propertyRepository.getById(propertyValue.propertyId)
                if (property?.type == PropertyType.IMAGE) {
                    propertyValue.value?.let { File(it) }?.delete()
                }
                languagePropertyValueRepository.delete(propertyValue)
            }
        }
        val terms = termRepository.getAll(entry.id)
        for (term in terms) {
            val termPropertyValues = termPropertyValueRepository.getAll(term.id)
            for (propertyValue in termPropertyValues) {
                val property = propertyRepository.getById(propertyValue.propertyId)
                if (property?.type == PropertyType.IMAGE) {
                    propertyValue.value?.let { File(it) }?.delete()
                }
                termPropertyValueRepository.delete(propertyValue)
            }
            termRepository.delete(term)
        }
        entryRepository.delete(entry)
    }
}
