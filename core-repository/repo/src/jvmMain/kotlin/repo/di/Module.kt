package repo.di

import persistence.dao.EntryDAO
import persistence.dao.EntryPropertyValueDAO
import persistence.dao.InputDescriptorDAO
import persistence.dao.LanguageDAO
import persistence.dao.LanguagePropertyValueDAO
import persistence.dao.PicklistValueDAO
import persistence.dao.PropertyDAO
import persistence.dao.TermDAO
import persistence.dao.TermPropertyValueDAO
import persistence.dao.TermbaseDAO
import org.koin.dsl.module
import repo.EntryPropertyValueRepository
import repo.EntryRepository
import repo.FlagsRepository
import repo.InputDescriptorRepository
import repo.LanguageNameRepository
import repo.LanguagePropertyValueRepository
import repo.LanguageRepository
import repo.PropertyRepository
import repo.TermPropertyValueRepository
import repo.TermRepository
import repo.TermbaseRepository

val repoModule = module {
    single {
        LanguageRepository(languageDAO = get())
    }
    single { FlagsRepository() }
    single { LanguageNameRepository() }
    single {
        TermbaseRepository(termbaseDAO = get())
    }
    single {
        EntryRepository(entryDAO = get())
    }
    single {
        TermRepository(termDAO = get())
    }
    single {
        EntryPropertyValueRepository(entryPropertyValueDAO = get())
    }
    single {
        LanguagePropertyValueRepository(languagePropertyValueDAO = get())
    }
    single {
        TermPropertyValueRepository(termPropertyValueDAO = get())
    }
    single {
        PropertyRepository(
            propertyDAO = get(),
            picklistValueDAO = get(),
        )
    }
    single {
        InputDescriptorRepository(inputDescriptorDAO = get())
    }
}
