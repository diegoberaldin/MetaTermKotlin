package usecase

import data.EntryModel
import data.PropertyType
import data.TermbaseModel
import repository.EntryPropertyValueRepository
import repository.EntryRepository
import repository.LanguagePropertyValueRepository
import repository.LanguageRepository
import repository.PropertyRepository
import repository.TermPropertyValueRepository
import repository.TermRepository
import repository.TermbaseRepository
import java.io.File

class DeleteTermbaseUseCase(
    private val termbaseRepository: TermbaseRepository,
    private val languageRepository: LanguageRepository,
    private val entryRepository: EntryRepository,
    private val termRepository: TermRepository,
    private val entryPropertyValueRepository: EntryPropertyValueRepository,
    private val languagePropertyValueRepository: LanguagePropertyValueRepository,
    private val termPropertyValueRepository: TermPropertyValueRepository,
    private val propertyRepository: PropertyRepository,
) {

    suspend operator fun invoke(termbase: TermbaseModel) {
        val entries = entryRepository.getAll(termbase.id)
        for (entry in entries) {
            deleteEntry(entry)
        }
        val languages = languageRepository.getAll(termbase.id)
        for (language in languages) {
            languageRepository.delete(language)
        }

        val properties = propertyRepository.getAll(termbase.id)
        for (property in properties) {
            propertyRepository.delete(property)
        }

        termbaseRepository.delete(termbase)
    }

    private suspend fun deleteEntry(entry: EntryModel) {
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
        val terms = termRepository.getAll(entryId = entry.id)
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
    }
}
