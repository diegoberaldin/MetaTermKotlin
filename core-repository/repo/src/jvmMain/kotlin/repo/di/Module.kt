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
        val dao: LanguageDAO by inject()
        LanguageRepository(languageDAO = dao)
    }
    single { FlagsRepository() }
    single { LanguageNameRepository() }
    single {
        val dao: TermbaseDAO by inject()
        TermbaseRepository(termbaseDAO = dao)
    }
    single {
        val dao: EntryDAO by inject()
        EntryRepository(entryDAO = dao)
    }
    single {
        val dao: TermDAO by inject()
        TermRepository(termDAO = dao)
    }
    single {
        val dao: EntryPropertyValueDAO by inject()
        EntryPropertyValueRepository(entryPropertyValueDAO = dao)
    }
    single {
        val dao: LanguagePropertyValueDAO by inject()
        LanguagePropertyValueRepository(languagePropertyValueDAO = dao)
    }
    single {
        val dao: TermPropertyValueDAO by inject()
        TermPropertyValueRepository(termPropertyValueDAO = dao)
    }
    single {
        val dao: PropertyDAO by inject()
        val valueDao: PicklistValueDAO by inject()
        PropertyRepository(
            propertyDAO = dao,
            picklistValueDAO = valueDao,
        )
    }
    single {
        val dao: InputDescriptorDAO by inject()
        InputDescriptorRepository(inputDescriptorDAO = dao)
    }
}
