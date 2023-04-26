package dao

import data.PropertyValueModel
import entities.LanguagePropertyValueEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class LanguagePropertyValueDAO {

    suspend fun getAll(languageId: Int, entryId: Int) = newSuspendedTransaction {
        LanguagePropertyValueEntity.select(
            (LanguagePropertyValueEntity.languageId eq languageId) and (LanguagePropertyValueEntity.entryId eq entryId),
        ).map {
            it.toPropertyValueModel()
        }
    }

    suspend fun getById(valueId: Int) = newSuspendedTransaction {
        LanguagePropertyValueEntity.select(LanguagePropertyValueEntity.id eq valueId).singleOrNull()
            ?.toPropertyValueModel()
    }

    suspend fun create(model: PropertyValueModel, languageId: Int, entryId: Int) = newSuspendedTransaction {
        LanguagePropertyValueEntity.insert {
            it[LanguagePropertyValueEntity.languageId] = languageId
            it[value] = model.value
            it[LanguagePropertyValueEntity.entryId] = entryId
            it[propertyId] = model.propertyId
        }[LanguagePropertyValueEntity.id].value
    }

    suspend fun update(model: PropertyValueModel) = newSuspendedTransaction {
        LanguagePropertyValueEntity.update({ LanguagePropertyValueEntity.id eq model.id }) {
            it[value] = model.value
        }
    }

    suspend fun delete(model: PropertyValueModel) = newSuspendedTransaction {
        LanguagePropertyValueEntity.deleteWhere { id eq model.id }
    }

    private fun ResultRow.toPropertyValueModel() = PropertyValueModel(
        id = this[LanguagePropertyValueEntity.id].value,
        value = this[LanguagePropertyValueEntity.value],
        propertyId = this[LanguagePropertyValueEntity.propertyId].value,
    )
}
