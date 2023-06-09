package common.utils

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.timeout
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent
import javax.swing.SwingUtilities
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var error: Throwable? = null
    var result: T? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (e: Throwable) {
            error = e
        }
    }

    error?.also { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}

inline fun <reified T> getByInjection(): T {
    val res by KoinJavaComponent.inject<T>(T::class.java)
    return res
}

inline fun <reified T> getByInjection(vararg params: Any?): T {
    val res by KoinJavaComponent.inject<T>(T::class.java) {
        parametersOf(*params)
    }
    return res
}

@OptIn(FlowPreview::class)
inline fun <reified T> Value<ChildStack<*, *>>.activeAsFlow(
    withNullsIfNotInstance: Boolean = false,
    timeout: Duration = 500.milliseconds,
): Flow<T?> = callbackFlow {
    val observer: (ChildStack<*, *>) -> Unit = {
        val child = it.active.instance
        if (T::class.java.isInstance(child)) {
            trySend(child as T)
        } else {
            if (withNullsIfNotInstance) {
                trySend(null)
            }
        }
    }
    subscribe(observer)
    awaitClose {
        unsubscribe(observer)
    }
}.timeout(timeout).catch { emit(null) }

@OptIn(FlowPreview::class)
inline fun <reified T> Value<ChildSlot<*, *>>.asFlow(
    withNullsIfNotInstance: Boolean = false,
    timeout: Duration = 500.milliseconds,
): Flow<T?> = callbackFlow {
    val observer: (ChildSlot<*, *>) -> Unit = {
        val child = it.child?.instance
        if (T::class.java.isInstance(child)) {
            trySend(child as T)
        } else {
            if (withNullsIfNotInstance) {
                trySend(null)
            }
        }
    }
    subscribe(observer)
    awaitClose {
        unsubscribe(observer)
    }
}.timeout(timeout).catch { emit(null) }
