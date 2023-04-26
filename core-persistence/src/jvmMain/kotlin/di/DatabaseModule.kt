package di

import AppDatabase
import org.koin.dsl.module

private val innerDbModule = module {
    single {
        AppDatabase(fileManager = get())
    }
}

val databaseKoinModule = module {
    includes(innerDbModule)

    single {
        val db: AppDatabase = get()
        db.termbaseDao()
    }
    single {
        val db: AppDatabase = get()
        db.entryDao()
    }
    single {
        val db: AppDatabase = get()
        db.languageDao()
    }
    single {
        val db: AppDatabase = get()
        db.termDao()
    }
    single {
        val db: AppDatabase = get()
        db.entryProperyValueDao()
    }
    single {
        val db: AppDatabase = get()
        db.languagePropertyValueDao()
    }
    single {
        val db: AppDatabase = get()
        db.termPropertyValueDao()
    }
    single {
        val db: AppDatabase = get()
        db.propertyDao()
    }
    single {
        val db: AppDatabase = get()
        db.picklistValueDao()
    }
    single {
        val db: AppDatabase = get()
        db.inputDescriptorDao()
    }
}
