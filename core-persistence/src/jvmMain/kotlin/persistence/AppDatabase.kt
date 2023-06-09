package persistence

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
import persistence.entities.EntryEntity
import persistence.entities.EntryPropertyValueEntity
import persistence.entities.InputDescriptorEntity
import persistence.entities.LanguageEntity
import persistence.entities.LanguagePropertyValueEntity
import persistence.entities.PicklistValueEntity
import persistence.entities.PropertyEntity
import persistence.entities.TermEntity
import persistence.entities.TermPropertyValueEntity
import persistence.entities.TermbaseEntity
import common.files.FileManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class AppDatabase(
    private val filename: String = FILE_NAME,
    private val fileManager: FileManager,
) {
    companion object {
        private const val DRIVER = "org.h2.Driver"
        private const val PROTO = "h2:file"
        private const val EXTRA_PARAMS = ";MODE=MYSQL"
        private const val FILE_NAME = "main"
    }

    init {
        setup()
    }

    private fun setup() {
        val appFileName = fileManager.getFilePath(filename)
        Database.connect("jdbc:$PROTO:$appFileName$EXTRA_PARAMS", driver = DRIVER)

        transaction {
            SchemaUtils.create(
                TermbaseEntity,
                LanguageEntity,
                LanguagePropertyValueEntity,
                EntryEntity,
                EntryPropertyValueEntity,
                TermEntity,
                TermPropertyValueEntity,
                PropertyEntity,
                PicklistValueEntity,
                InputDescriptorEntity,
            )
        }
    }

    fun termbaseDao() = TermbaseDAO()

    fun entryDao() = EntryDAO()

    fun languageDao() = LanguageDAO()

    fun termDao() = TermDAO()

    fun entryPropertyValueDao() = EntryPropertyValueDAO()

    fun languagePropertyValueDao() = LanguagePropertyValueDAO()

    fun termPropertyValueDao() = TermPropertyValueDAO()

    fun propertyDao() = PropertyDAO()

    fun picklistValueDao() = PicklistValueDAO()

    fun inputDescriptorDao() = InputDescriptorDAO()
}
