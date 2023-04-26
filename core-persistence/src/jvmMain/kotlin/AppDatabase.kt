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
import entities.EntryEntity
import entities.EntryPropertyValueEntity
import entities.InputDescriptorEntity
import entities.LanguageEntity
import entities.LanguagePropertyValueEntity
import entities.PicklistValueEntity
import entities.PropertyEntity
import entities.TermEntity
import entities.TermPropertyValueEntity
import entities.TermbaseEntity
import files.FileManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

internal class AppDatabase(
    private val fileManager: FileManager,
) {
    companion object {
        private const val DRIVER = "org.h2.Driver"
        private const val PROTO = "h2:file"
        private const val EXTRA_PARAMS = ";MODE=MYSQL"
        private const val FILE_NAME = "main"
    }

    private val setup by lazy {
        val appFileName = fileManager.getFilePath(FILE_NAME)
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

    init {
        setup
    }

    fun termbaseDao() = TermbaseDAO()

    fun entryDao() = EntryDAO()

    fun languageDao() = LanguageDAO()

    fun termDao() = TermDAO()

    fun entryProperyValueDao() = EntryPropertyValueDAO()

    fun languagePropertyValueDao() = LanguagePropertyValueDAO()

    fun termPropertyValueDao() = TermPropertyValueDAO()

    fun propertyDao() = PropertyDAO()

    fun picklistValueDao() = PicklistValueDAO()

    fun inputDescriptorDao() = InputDescriptorDAO()
}
