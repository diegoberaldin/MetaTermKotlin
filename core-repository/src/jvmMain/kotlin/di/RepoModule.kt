package di

import dao.EntryDAO
import dao.EntryPropertyValueDAO
import dao.InputDescriptorDAO
import dao.LanguageDAO
import dao.LanguagePropertyValueDAO
import dao.PicklistValueDAO
import dao.PropertyDAO
import dao.TermDAO
import dao.TermPropertyValueDAO
import dao.TermbaseDAO
import org.koin.dsl.module
import repository.EntryPropertyValueRepository
import repository.EntryRepository
import repository.FlagsRepository
import repository.InputDescriptorRepository
import repository.LanguageNameRepository
import repository.LanguagePropertyValueRepository
import repository.LanguageRepository
import repository.PropertyRepository
import repository.TermPropertyValueRepository
import repository.TermRepository
import repository.TermbaseRepository

internal val repoKoinModule = module {
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
