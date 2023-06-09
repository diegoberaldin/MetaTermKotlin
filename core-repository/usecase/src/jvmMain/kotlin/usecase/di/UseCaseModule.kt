package usecase.di

import org.koin.dsl.module
import repo.EntryPropertyValueRepository
import repo.EntryRepository
import repo.LanguagePropertyValueRepository
import repo.LanguageRepository
import repo.PropertyRepository
import repo.TermPropertyValueRepository
import repo.TermRepository
import repo.TermbaseRepository
import usecase.DeleteEntryUseCase
import usecase.DeleteTermUseCase
import usecase.DeleteTermbaseLanguageUseCase
import usecase.DeleteTermbaseUseCase
import usecase.ExportCsvUseCase
import usecase.ExportTbxUseCase
import usecase.ImportCsvUseCase
import usecase.ImportTbxUseCase
import usecase.SearchTermsUseCase

val usecaseModule = module {
    single {
        val entryRepository: EntryRepository by inject()
        val languageRepository: LanguageRepository by inject()
        val languagePropertyValueRepository: LanguagePropertyValueRepository by inject()
        val termRepository: TermRepository by inject()
        val termPropertyValueRepository: TermPropertyValueRepository by inject()
        DeleteTermbaseLanguageUseCase(
            entryRepository = entryRepository,
            languageRepository = languageRepository,
            languagePropertyValueRepository = languagePropertyValueRepository,
            termRepository = termRepository,
            termPropertyValueRepository = termPropertyValueRepository,
        )
    }
    single {
        val termRepository: TermRepository by inject()
        val termPropertyValueRepository: TermPropertyValueRepository by inject()
        DeleteTermUseCase(
            termRepository = termRepository,
            termPropertyValueRepository = termPropertyValueRepository,
        )
    }
    single {
        val termbaseRepository: TermbaseRepository by inject()
        val entryRepository: EntryRepository by inject()
        val entryPropertyValueRepository: EntryPropertyValueRepository by inject()
        val propertyRepository: PropertyRepository by inject()
        val languageRepository: LanguageRepository by inject()
        val languagePropertyValueRepository: LanguagePropertyValueRepository by inject()
        val termRepository: TermRepository by inject()
        val termPropertyValueRepository: TermPropertyValueRepository by inject()
        DeleteTermbaseUseCase(
            entryRepository = entryRepository,
            entryPropertyValueRepository = entryPropertyValueRepository,
            propertyRepository = propertyRepository,
            languageRepository = languageRepository,
            languagePropertyValueRepository = languagePropertyValueRepository,
            termRepository = termRepository,
            termPropertyValueRepository = termPropertyValueRepository,
            termbaseRepository = termbaseRepository,
        )
    }
    single {
        val entryRepository: EntryRepository by inject()
        val entryPropertyValueRepository: EntryPropertyValueRepository by inject()
        val propertyRepository: PropertyRepository by inject()
        val languageRepository: LanguageRepository by inject()
        val languagePropertyValueRepository: LanguagePropertyValueRepository by inject()
        val termRepository: TermRepository by inject()
        val termPropertyValueRepository: TermPropertyValueRepository by inject()
        DeleteEntryUseCase(
            entryRepository = entryRepository,
            entryPropertyValueRepository = entryPropertyValueRepository,
            propertyRepository = propertyRepository,
            languageRepository = languageRepository,
            languagePropertyValueRepository = languagePropertyValueRepository,
            termRepository = termRepository,
            termPropertyValueRepository = termPropertyValueRepository,
        )
    }
    single {
        val entryRepository: EntryRepository by inject()
        val termRepository: TermRepository by inject()
        val propertyRepository: PropertyRepository by inject()
        val termPropertyValueRepository: TermPropertyValueRepository by inject()
        ExportTbxUseCase(
            termRepository = termRepository,
            entryRepository = entryRepository,
            propertyRepository = propertyRepository,
            termPropertyValueRepository = termPropertyValueRepository,
        )
    }
    single {
        val entryRepository: EntryRepository by inject()
        val languageRepository: LanguageRepository by inject()
        val termRepository: TermRepository by inject()
        ExportCsvUseCase(
            termRepository = termRepository,
            entryRepository = entryRepository,
            languageRepository = languageRepository,
        )
    }
    single {
        val entryRepository: EntryRepository by inject()
        val languageRepository: LanguageRepository by inject()
        val termRepository: TermRepository by inject()
        ImportCsvUseCase(
            termRepository = termRepository,
            entryRepository = entryRepository,
            languageRepository = languageRepository,
        )
    }
    single {
        val entryRepository: EntryRepository by inject()
        val languageRepository: LanguageRepository by inject()
        val termRepository: TermRepository by inject()
        val propertyRepository: PropertyRepository by inject()
        val termPropertyValueRepository: TermPropertyValueRepository by inject()
        ImportTbxUseCase(
            termRepository = termRepository,
            entryRepository = entryRepository,
            languageRepository = languageRepository,
            propertyRepository = propertyRepository,
            termPropertyValueRepository = termPropertyValueRepository,
        )
    }
    single {
        val termRepository: TermRepository by inject()
        SearchTermsUseCase(termRepository = termRepository)
    }
}
