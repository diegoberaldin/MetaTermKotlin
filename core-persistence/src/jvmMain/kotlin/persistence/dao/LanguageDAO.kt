package persistence.dao

import data.LanguageModel
import persistence.entities.LanguageEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class LanguageDAO {

    suspend fun create(model: LanguageModel) = newSuspendedTransaction {
        LanguageEntity.insertIgnore {
            it[code] = model.code
            it[termbaseId] = model.termbaseId
        }[LanguageEntity.id].value
    }

    suspend fun delete(model: LanguageModel) = newSuspendedTransaction {
        LanguageEntity.deleteWhere {
            (code eq model.code) and (termbaseId eq termbaseId)
        }
    }

    suspend fun getAll(termbaseId: Int) = newSuspendedTransaction {
        LanguageEntity.select(LanguageEntity.termbaseId eq termbaseId).map {
            it.toLanguageModel()
        }
    }

    suspend fun getById(languageId: Int): LanguageModel? = newSuspendedTransaction {
        val entity = LanguageEntity.select(LanguageEntity.id eq languageId).singleOrNull()
        entity?.toLanguageModel()
    }

    suspend fun getByCode(code: String, termbaseId: Int): LanguageModel? = newSuspendedTransaction {
        val entity = LanguageEntity.select((LanguageEntity.termbaseId eq termbaseId) and (LanguageEntity.code eq code))
            .singleOrNull()
        entity?.toLanguageModel()
    }

    private fun ResultRow.toLanguageModel() =
        LanguageModel(
            id = this[LanguageEntity.id].value,
            code = this[LanguageEntity.code],
            termbaseId = this[LanguageEntity.termbaseId].value,
        )
}
