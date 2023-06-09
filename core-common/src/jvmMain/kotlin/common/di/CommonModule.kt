package common.di

import common.files.DefaultFileManager
import common.files.FileManager
import common.keystore.DefaultTemporaryKeyStore
import common.keystore.TemporaryKeyStore
import common.log.DefaultLogManager
import common.log.LogManager
import common.notification.DefaultNotificationCenter
import common.notification.NotificationCenter
import org.koin.dsl.module
import common.coroutines.CoroutineDispatcherProvider
import common.coroutines.CoroutineDispatcherProviderImpl

val commonModule = module {
    single<CoroutineDispatcherProvider> {
        CoroutineDispatcherProviderImpl
    }
    single<NotificationCenter> {
        DefaultNotificationCenter
    }
    single<TemporaryKeyStore> {
        DefaultTemporaryKeyStore(fileManager = get())
    }
    single<FileManager> {
        DefaultFileManager
    }
    single<LogManager> {
        DefaultLogManager(fileManager = get())
    }
}
