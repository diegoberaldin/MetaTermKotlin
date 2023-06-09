package repository.di

import org.koin.dsl.module
import usecase.di.usecaseModule
import repo.di.repoModule

val repositoryModule = module {
    includes(
        repoModule,
        usecaseModule,
    )
}