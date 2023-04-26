package dao

import data.InputDescriptorModel
import entities.InputDescriptorEntity
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class InputDescriptorDAO {
    suspend fun create(model: InputDescriptorModel) = newSuspendedTransaction {
        InputDescriptorEntity.insertIgnore {
            it[lang] = model.lang
            it[lemma] = model.lemma
            it[propertyId] = model.propertyId
            it[termbaseId] = model.termbaseId
        }[InputDescriptorEntity.id].value
    }

    suspend fun update(model: InputDescriptorModel) = newSuspendedTransaction {
        InputDescriptorEntity.update({ InputDescriptorEntity.id eq model.id }) {
            it[lang] = model.lang
            it[lemma] = model.lemma
            it[propertyId] = model.propertyId
            it[termbaseId] = model.termbaseId
        }
    }

    suspend fun delete(model: InputDescriptorModel) = newSuspendedTransaction {
        InputDescriptorEntity.deleteWhere { InputDescriptorEntity.id eq model.id }
    }

    suspend fun getAll(termbaseId: Int) = newSuspendedTransaction {
        InputDescriptorEntity.select(InputDescriptorEntity.termbaseId eq termbaseId).map {
            it.toInputDescriptorModel()
        }
    }

    suspend fun getById(id: Int) = newSuspendedTransaction {
        InputDescriptorEntity.select(InputDescriptorEntity.id eq id).singleOrNull()?.toInputDescriptorModel()
    }

    private fun ResultRow.toInputDescriptorModel() = InputDescriptorModel(
        id = this[InputDescriptorEntity.id].value,
        termbaseId = this[InputDescriptorEntity.termbaseId].value,
        lemma = this.getOrNull(InputDescriptorEntity.lemma),
        propertyId = this.getOrNull(InputDescriptorEntity.propertyId)?.value,
        lang = this.getOrNull(InputDescriptorEntity.lang),
    )
}
