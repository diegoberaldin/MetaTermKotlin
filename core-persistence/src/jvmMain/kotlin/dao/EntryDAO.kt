package dao

import data.EntryModel
import entities.EntryEntity
import entities.EntryEntity.termbaseId
import entities.TermEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class EntryDAO {

    suspend fun create(model: EntryModel) = newSuspendedTransaction {
        EntryEntity.insertIgnore {
            it[termbaseId] = model.termbaseId
        }[EntryEntity.id].value
    }

    suspend fun delete(model: EntryModel) = newSuspendedTransaction {
        EntryEntity.deleteWhere { EntryEntity.id eq model.id }
    }

    suspend fun getById(entryId: Int): EntryModel? = newSuspendedTransaction {
        val result = EntryEntity.select { EntryEntity.id eq entryId }.singleOrNull()
        result?.toEntryModel()
    }

    suspend fun getAll(termbaseId: Int): List<EntryModel> = newSuspendedTransaction {
        EntryEntity.select { EntryEntity.termbaseId eq termbaseId }.map {
            it.toEntryModel()
        }
    }

    suspend fun countAll(termbaseId: Int) = newSuspendedTransaction {
        EntryEntity.select { EntryEntity.termbaseId eq termbaseId }.count()
    }

    suspend fun countComplete(code: String, termbaseId: Int): Long = newSuspendedTransaction {
        EntryEntity
            .select {
                EntryEntity.termbaseId eq termbaseId and exists(TermEntity.select { (TermEntity.entryId eq EntryEntity.id) and (TermEntity.lang eq code) })
            }
            .count()
    }

    fun observeAll(termbaseId: Int): Flow<List<EntryModel>> = channelFlow {
        while (true) {
            if (isActive) {
                val result = getAll(termbaseId)
                trySend(result)
                delay(1000)
            }
        }
    }

    private fun ResultRow.toEntryModel() = EntryModel(
        id = this[EntryEntity.id].value,
        termbaseId = this[termbaseId].value,
    )
}
